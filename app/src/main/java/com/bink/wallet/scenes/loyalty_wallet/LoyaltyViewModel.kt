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
import com.bink.wallet.utils.JOIN_CARD
import com.bink.wallet.utils.enums.CardType
import kotlinx.coroutines.launch

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

    val localPaymentCards: LiveData<List<PaymentCard>>
        get() = _localPaymentCards
    private val _localPaymentCards = MutableLiveData<List<PaymentCard>>()

    val dismissedCardData = MutableLiveData<List<BannerDisplay>>()
    private val _addError = MutableLiveData<Throwable>()
    val addError: LiveData<Throwable>
        get() = _addError
    private val _fetchError = MutableLiveData<Throwable>()
    val fetchError: LiveData<Throwable>
        get() = _fetchError
    private val _loadPlansError = MutableLiveData<Throwable>()
    val loadPlansError: MutableLiveData<Throwable>
        get() = _loadPlansError
    private val _loadCardsError = MutableLiveData<Throwable>()
    val loadCardsError: LiveData<Throwable>
        get() = _loadCardsError
    private val _deleteCardError = MutableLiveData<Throwable>()
    val deleteCardError: MutableLiveData<Throwable>
        get() = _deleteCardError

    private val _cardsDataMerger = MediatorLiveData<UserDataResult>()
    val cardsDataMerger: LiveData<UserDataResult>
        get() = _cardsDataMerger

    private val _dismissedBannerDisplay = MutableLiveData<String>()
    val dismissedBannerDisplay: LiveData<String>
        get() = _dismissedBannerDisplay

    private val _localCardsDataMerger = MediatorLiveData<UserDataResult>()
    val localCardsDataMerger: LiveData<UserDataResult>
        get() = _localCardsDataMerger

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

        _localCardsDataMerger.addSource(localMembershipCardData) {
            _localCardsDataMerger.value =
                combineCardsData(
                    localMembershipCardData,
                    localMembershipPlanData,
                    dismissedCardData
                )
        }

        _localCardsDataMerger.addSource(localMembershipPlanData) {
            _localCardsDataMerger.value =
                combineCardsData(
                    localMembershipCardData,
                    localMembershipPlanData,
                    dismissedCardData
                )
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
        val walletItems = ArrayList<Any>(plansReceivedValue.filter {
            it.getCardType() == CardType.PLL &&
                    merchantNoLoyalty(cardsReceivedValue, it) &&
                    dismissedCardsValue.firstOrNull { currentId ->
                        it.id == currentId.id
                    } == null
        })
        if (dismissedCardsValue.firstOrNull { it.id == JOIN_CARD } == null &&
            SharedPreferenceManager.isPaymentEmpty) {
            walletItems.add(JoinCardItem())
        }
        walletItems.addAll(cardsReceivedValue)
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
        viewModelScope.launch {
            loyaltyWalletRepository.retrieveMembershipCards(membershipCardData, _loadCardsError)
        }
    }

    fun fetchMembershipPlans() {
        viewModelScope.launch {
            loyaltyWalletRepository.retrieveMembershipPlans(membershipPlanData, loadPlansError)
        }
    }

    fun fetchLocalMembershipCards() {
        loyaltyWalletRepository.retrieveStoredMembershipCards(localMembershipCardData)
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
