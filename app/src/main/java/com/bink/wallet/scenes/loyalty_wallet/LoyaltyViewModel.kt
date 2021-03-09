package com.bink.wallet.scenes.loyalty_wallet

import android.content.Context
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.bink.wallet.BaseViewModel
import com.bink.wallet.data.SharedPreferenceManager
import com.bink.wallet.model.BannerDisplay
import com.bink.wallet.model.JoinCardItem
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_card.UserDataResult
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.model.response.payment_card.PaymentCard
import com.bink.wallet.scenes.pll.PaymentWalletRepository
import com.bink.wallet.utils.DateTimeUtils
import com.bink.wallet.utils.JOIN_CARD
import com.bink.wallet.utils.LocalPointScraping.WebScrapableManager
import com.bink.wallet.utils.enums.CardType
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoyaltyViewModel constructor(
    private val loyaltyWalletRepository: LoyaltyWalletRepository,
    private val paymentWalletRepository: PaymentWalletRepository,
    private var zendeskRepository: ZendeskRepository
) :
    BaseViewModel() {

    val membershipCardData = MutableLiveData<List<MembershipCard>>()
    val deleteCard = MutableLiveData<String>()
    val membershipPlanData = MutableLiveData<List<MembershipPlan>>()
    val localMembershipPlanData = MutableLiveData<List<MembershipPlan>>()

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    private val _localPaymentCards = MutableLiveData<List<PaymentCard>>()
    val localPaymentCards: LiveData<List<PaymentCard>>
        get() = _localPaymentCards
    val dismissedCardData = MutableLiveData<List<BannerDisplay>>()
    private val _addError = MutableLiveData<Exception>()
    val addError: LiveData<Exception>
        get() = _addError
    private val _fetchError = MutableLiveData<Exception>()
    val fetchError: LiveData<Exception>
        get() = _fetchError
    private val _loadPlansError = MutableLiveData<Exception>()
    val loadPlansError: MutableLiveData<Exception>
        get() = _loadPlansError
    private val _loadCardsError = MutableLiveData<Exception>()
    val loadCardsError: LiveData<Exception>
        get() = _loadCardsError
    private val _deleteCardError = MutableLiveData<Exception>()
    val deleteCardError: MutableLiveData<Exception>
        get() = _deleteCardError
    private val _cardsDataMerger = MediatorLiveData<UserDataResult>()
    val cardsDataMerger: LiveData<UserDataResult>
        get() = _cardsDataMerger
    private val _dismissedBannerDisplay = MutableLiveData<String>()
    val dismissedBannerDisplay: LiveData<String>
        get() = _dismissedBannerDisplay
    private val job = Job()
    private val scope = CoroutineScope(job + Dispatchers.Main)
    private val _hasZendeskResponse = MutableLiveData<Boolean>()
    val hasZendeskResponse: LiveData<Boolean> get() = _hasZendeskResponse

    init {
        _cardsDataMerger.addSource(membershipCardData) {
            _cardsDataMerger.value =
                combineCardsData(membershipCardData, membershipPlanData, dismissedCardData)
        }

        _cardsDataMerger.addSource(membershipPlanData) {
            _cardsDataMerger.value =
                combineCardsData(membershipCardData, membershipPlanData, dismissedCardData)
        }

        _cardsDataMerger.addSource(dismissedCardData) {
            _cardsDataMerger.value =
                combineCardsData(membershipCardData, membershipPlanData, dismissedCardData)
        }
    }

    fun checkZendeskResponse() {
        _hasZendeskResponse.value = zendeskRepository.hasResponseBeenReceived()
    }

    private fun combineCardsData(
        cardsReceived: LiveData<List<MembershipCard>>,
        plansReceived: LiveData<List<MembershipPlan>>,
        dismissedCards: LiveData<List<BannerDisplay>>
    ): UserDataResult {
        val cardsReceivedValue = cardsReceived.value
        val plansReceivedValue = plansReceived.value
        val dismissedCardsValue = dismissedCards.value
        if (cardsReceivedValue == null || plansReceivedValue == null || dismissedCardsValue == null) {
            return UserDataResult.UserDataLoading
        }
        val walletItems = arrayListOf<Any>()
        walletItems.addAll(cardsReceivedValue)
        walletItems.addAll(
            plansReceivedValue.filter { membershipPlan ->
                membershipPlan.getCardType() == CardType.PLL &&
                        merchantNoLoyalty(cardsReceivedValue, membershipPlan) &&
                        dismissedCardsValue.firstOrNull { currentId ->
                            membershipPlan.id == currentId.id
                        } == null
            }.sortedBy { it.account?.company_name }
        )
        if (dismissedCardsValue.firstOrNull { it.id == JOIN_CARD } == null &&
            SharedPreferenceManager.isPaymentEmpty) {
            walletItems.add(JoinCardItem())
        }
        return UserDataResult.UserDataSuccess(
            Triple(
                cardsReceivedValue,
                plansReceivedValue,
                walletItems
            )
        )
    }

    private fun merchantNoLoyalty(
        cardsReceived: List<MembershipCard>,
        plan: MembershipPlan
    ): Boolean {
        return cardsReceived.firstOrNull { card ->
            card.membership_plan == plan.id
        } == null
    }

    fun deleteCard(id: String?) {
        loyaltyWalletRepository.deleteMembershipCard(id, deleteCard, deleteCardError)
    }

    fun fetchMembershipCards(context: Context?) {
        loyaltyWalletRepository.retrieveMembershipCards(membershipCardData, _loadCardsError) {
            checkCardScrape(it, context)
        }
    }

    private fun checkCardScrape(cards: List<MembershipCard>, context: Context?) {
        val shouldScrapeCards = DateTimeUtils.haveTwoHoursElapsed(SharedPreferenceManager.membershipCardsLastScraped)

        if (shouldScrapeCards) {
            scrapeCards(cards, context)
            SharedPreferenceManager.membershipCardsLastScraped = System.currentTimeMillis()
        } else {
            fetchLocalMembershipCards { cardsFromDb ->
                setMembershipCardsFromDb(cardsFromDb, cards)
            }
        }
    }

    private fun setMembershipCardsFromDb(cardsFromDb: List<MembershipCard>, cards: List<MembershipCard>){
        membershipCardData.value = WebScrapableManager.mapOldToNewCards(cardsFromDb, cards)
    }

    fun fetchPeriodicMembershipCards(context: Context?) {
        val shouldMakePeriodicCall =
            DateTimeUtils.haveTwoMinutesElapsed(SharedPreferenceManager.membershipCardsLastRequestTime)
        if (shouldMakePeriodicCall) {
            fetchMembershipCards(context)
        } else {
            fetchLocalMembershipCards {
                checkCardScrape(it, context)
            }
        }
    }

    fun fetchMembershipPlans(fromPersistence: Boolean) {
        val handler = CoroutineExceptionHandler { _, _ -> //Exception handler to prevent app crash

        }
        scope.launch(handler) {
            loyaltyWalletRepository.retrieveMembershipPlans(
                membershipPlanData,
                loadPlansError,
                fromPersistence
            )
        }
    }

    fun fetchLocalMembershipCards(callback: ((List<MembershipCard>) -> Unit?)? = null) {
        loyaltyWalletRepository.retrieveStoredMembershipCards {
            membershipCardData.value = it
            callback?.let { returnData -> returnData(it) }
        }
    }

    fun fetchMembershipCardsAndPlansForRefresh(context: Context?) {
        val handler = CoroutineExceptionHandler { _, _ ->
            _isLoading.value = false

        }
        /**
         * Ideally viewModelScope should be used here as it cancels itself automatically when onCleared() is called.
         * Current Koin viewModel injection implementation doesn't reinitialise the viewModel even after onCleared(),
         * meaning that the coroutineScope would be cancelled an unable to do any work. There currently isn't a fix for this,
         * https://github.com/InsertKoinIO/koin/issues/506.
         * ViewModel initialisation in the LoyaltyWalletFragment might need to be changed from using by viewModel,to using getViewModel which will
         * always return a new instance.
         **/
        scope.launch(handler) {
            _isLoading.value = true

            try {
                val membershipCardsAndPlans = withContext(Dispatchers.IO) {
                    loyaltyWalletRepository.retrieveMembershipCardsAndPlans()
                }

                membershipPlanData.value = membershipCardsAndPlans.membershipPlans
                membershipCardData.value = membershipCardsAndPlans.membershipCards

                membershipCardsAndPlans.membershipCards?.let {
                    checkCardScrape(it, context)
                }

                _isLoading.value = false

            } catch (e: Exception) {
                _isLoading.value = false
                _loadPlansError.value = e
                _loadCardsError.value = e
            }
        }
    }

    private fun scrapeCards(cards: List<MembershipCard>, context: Context?) {
        WebScrapableManager.tryScrapeCards(0, cards, context, false) { scrapedCards ->
            if (scrapedCards != null) {
                updateScrapedCards(scrapedCards)
            }
        }
    }

    private fun updateScrapedCards(cards: List<MembershipCard>) {
        val scrapedCards = cards.filter { it.isScraped == true }
        for (card in scrapedCards) {
            loyaltyWalletRepository.storeMembershipCard(card)
        }
    }

    fun addNewlyScrapedCard(newlyAddedCard: MembershipCard) {
        val combinedCards = ArrayList<MembershipCard>()
        val previousCards = membershipCardData.value

        combinedCards.add(newlyAddedCard)

        previousCards?.let { cards ->
            combinedCards.addAll(cards.filter { it.id != newlyAddedCard?.id })
        }

        membershipCardData.value = combinedCards
    }

    fun fetchLocalMembershipPlans() {
        loyaltyWalletRepository.retrieveStoredMembershipPlans(localMembershipPlanData)
    }

    fun fetchDismissedCards() {
        loyaltyWalletRepository.retrieveDismissedCards(dismissedCardData, _fetchError)
    }

    fun fetchLocalPaymentCards() {
        paymentWalletRepository.getLocalPaymentCards(_localPaymentCards, _fetchError)
    }

    fun addPlanIdAsDismissed(id: String) {
        loyaltyWalletRepository.addBannerAsDismissed(id, _addError, _dismissedBannerDisplay)
    }
}
