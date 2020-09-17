package com.bink.wallet.scenes.loyalty_wallet

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
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
import com.bink.wallet.utils.enums.CardType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoyaltyViewModel constructor(
    private val loyaltyWalletRepository: LoyaltyWalletRepository,
    private val paymentWalletRepository: PaymentWalletRepository
) :
    BaseViewModel() {

    val membershipCardData = MutableLiveData<List<MembershipCard>>()
    val deleteCard = MutableLiveData<String>()
    val membershipPlanData = MutableLiveData<List<MembershipPlan>>()
    val localMembershipPlanData = MutableLiveData<List<MembershipPlan>>()
    val localMembershipCardData = MutableLiveData<List<MembershipCard>>()

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

    fun fetchMembershipCards() {
        loyaltyWalletRepository.retrieveMembershipCards(membershipCardData, _loadCardsError)
    }

    fun fetchPeriodicMembershipCards() {
        val shouldMakePeriodicCall =
            DateTimeUtils.haveTwoMinutesElapsed(SharedPreferenceManager.membershipCardsLastRequestTime)
        if (shouldMakePeriodicCall) {
            fetchMembershipCards()
        } else {
            fetchLocalMembershipCards(true)
        }
    }

    fun fetchMembershipPlans(fromPersistence: Boolean) {
        viewModelScope.launch {
            loyaltyWalletRepository.retrieveMembershipPlans(
                membershipPlanData,
                loadPlansError,
                fromPersistence
            )
        }
    }

    fun fetchLocalMembershipCards(isFromPeriodicCall: Boolean) {
        if (isFromPeriodicCall) {
            loyaltyWalletRepository.retrieveStoredMembershipCards(membershipCardData)
        } else {
            loyaltyWalletRepository.retrieveStoredMembershipCards(localMembershipCardData)
        }
    }

    fun fetchMembershipCardsAndPlansForRefresh() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val membershipCardsAndPlans = withContext(Dispatchers.IO) {
                    loyaltyWalletRepository.retrieveMembershipCardsAndPlans()
                }

                membershipPlanData.value = membershipCardsAndPlans.membershipPlans
                membershipCardData.value = membershipCardsAndPlans.membershipCards

                _isLoading.value = false

            } catch (e: Exception) {
                _isLoading.value = false
                _loadPlansError.value = e
                _loadCardsError.value = e
            }
        }
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
