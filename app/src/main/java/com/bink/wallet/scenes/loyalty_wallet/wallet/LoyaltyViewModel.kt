package com.bink.wallet.scenes.loyalty_wallet.wallet

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.bink.wallet.BaseViewModel
import com.bink.wallet.data.SharedPreferenceManager
import com.bink.wallet.model.BannerDisplay
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_card.UserDataResult
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.model.response.payment_card.PaymentCard
import com.bink.wallet.scenes.pll.PaymentWalletRepository
import com.bink.wallet.utils.DateTimeUtils
import com.bink.wallet.utils.UtilFunctions
import com.bink.wallet.utils.local_point_scraping.WebScrapableManager
import com.bink.wallet.utils.logDebug
import kotlinx.coroutines.*

class LoyaltyViewModel constructor(
    private val loyaltyWalletRepository: LoyaltyWalletRepository,
    private val paymentWalletRepository: PaymentWalletRepository
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
        walletItems.addAll(plansReceivedValue)

        return UserDataResult.UserDataSuccess(
            Triple(
                cardsReceivedValue,
                plansReceivedValue,
                walletItems
            )
        )
    }

    fun deleteCard(id: String?) {
        loyaltyWalletRepository.deleteMembershipCard(id, deleteCard, deleteCardError)
    }

    fun fetchMembershipCards(
        context: Context?,
        lpsCardStatus: (Boolean, String, Boolean, String?) -> Unit
    ) {
        loyaltyWalletRepository.retrieveMembershipCards(membershipCardData, _loadCardsError) {
            checkCardScrape(it, context, lpsCardStatus)
        }
    }

    private fun checkCardScrape(
        cards: List<MembershipCard>,
        context: Context?,
        lpsCardStatus: (Boolean, String, Boolean, String?) -> Unit
    ) {
        val shouldScrapeCards =
            DateTimeUtils.haveTwelveHoursElapsed(SharedPreferenceManager.membershipCardsLastScraped) && UtilFunctions.isNetworkAvailable(
                context
            )

        if (shouldScrapeCards) {
            scrapeCards(cards, context, lpsCardStatus)
            SharedPreferenceManager.membershipCardsLastScraped = System.currentTimeMillis()
        } else {
            fetchLocalMembershipCards { cardsFromDb ->
                setMembershipCardsFromDb(cardsFromDb, cards)
            }
        }
    }

    private fun setMembershipCardsFromDb(
        cardsFromDb: List<MembershipCard>,
        cards: List<MembershipCard>
    ) {
        membershipCardData.value = WebScrapableManager.mapOldToNewCards(cardsFromDb, cards)
    }

    fun fetchPeriodicMembershipCards(
        context: Context,
        lpsCardStatus: (Boolean, String, Boolean, String?) -> Unit
    ) {
        val shouldMakePeriodicCall =
            DateTimeUtils.haveTwoMinutesElapsed(SharedPreferenceManager.membershipCardsLastRequestTime) && UtilFunctions.isNetworkAvailable(
                context
            )
        if (shouldMakePeriodicCall) {
            fetchMembershipCards(context, lpsCardStatus)
        } else {
            fetchLocalMembershipCards {
                checkCardScrape(it, context, lpsCardStatus)
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
        scope.launch {
            try {
                val localCards =
                    withContext(Dispatchers.IO) { loyaltyWalletRepository.retrieveStoredMembershipCards() }
                membershipCardData.value = localCards
                callback?.let { returnData -> returnData(localCards) }
            } catch (e: Exception) {
                logDebug("LoyaltyViewModel", e.message)
            }
        }

    }

    fun fetchMembershipCardsAndPlansForRefresh(
        context: Context?,
        lpsCardStatus: (Boolean, String, Boolean, String?) -> Unit
    ) {
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

                membershipCardsAndPlans.membershipPlans.let {
                    membershipPlanData.value = it
                }
                membershipCardsAndPlans.membershipCards.let {
                    membershipCardData.value = it
                }

                membershipCardsAndPlans.membershipCards?.let {
                    membershipCardData.value = it
                    checkCardScrape(it, context, lpsCardStatus)
                }

                _isLoading.value = false

            } catch (e: Exception) {
                _isLoading.value = false
                _loadPlansError.value = e
                _loadCardsError.value = e
            }
        }
    }

    private fun scrapeCards(
        cards: List<MembershipCard>,
        context: Context?,
        lpsCardStatus: (Boolean, String, Boolean, String?) -> Unit
    ) {
        WebScrapableManager.tryScrapeCards(
            0,
            cards,
            context,
            false,
            lpsCardStatus
        ) { scrapedCards ->
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
            combinedCards.addAll(cards.filter { it.id != newlyAddedCard.id })
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

}
