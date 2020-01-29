package com.bink.wallet.scenes.payment_card_wallet

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bink.wallet.BaseViewModel
import com.bink.wallet.model.BannerDisplay
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.model.response.payment_card.PaymentCard
import com.bink.wallet.scenes.loyalty_wallet.LoyaltyWalletRepository
import com.bink.wallet.scenes.pll.PaymentWalletRepository
import okhttp3.ResponseBody

class PaymentCardWalletViewModel(
    private var paymentWalletRepository: PaymentWalletRepository,
    private var loyaltyWalletRepository: LoyaltyWalletRepository
) : BaseViewModel() {
    val paymentCards = MutableLiveData<List<PaymentCard>>()
    val deleteCard = MutableLiveData<String>()
    val deleteRequest = MutableLiveData<ResponseBody>()
    val localMembershipPlanData = MutableLiveData<List<MembershipPlan>>()
    val localMembershipCardData = MutableLiveData<List<MembershipCard>>()
    val dismissedCardData = MutableLiveData<List<BannerDisplay>>()

    private val _fetchError = MutableLiveData<Throwable>()
    val fetchError : LiveData<Throwable>
        get() = _fetchError
    private val _deleteError = MutableLiveData<Throwable>()
    val deleteError : LiveData<Throwable>
        get() = _deleteError
    private val _deleteCardError = MutableLiveData<Throwable>()
    val deleteCardError : LiveData<Throwable>
        get() = _deleteCardError
    private val _addError = MutableLiveData<Throwable>()
    val addError : LiveData<Throwable>
        get() = _addError

    suspend fun deleteCard(id: String?) {
        loyaltyWalletRepository.deleteMembershipCard(
            id,
            deleteCard,
            _deleteCardError
        )
    }

    suspend fun getPaymentCards() {
        paymentWalletRepository.getPaymentCards(
            paymentCards,
            _fetchError
        )
    }

    fun fetchLocalPaymentCards() {
        paymentWalletRepository.getLocalPaymentCards(
            paymentCards,
            _fetchError
        )
    }

    fun fetchLocalMembershipCards() {
        loyaltyWalletRepository.retrieveStoredMembershipCards(
            localMembershipCardData
        )
    }

    fun fetchLocalMembershipPlans() {
        loyaltyWalletRepository.retrieveStoredMembershipPlans(
            localMembershipPlanData
        )
    }

    suspend fun deletePaymentCard(paymentCardId: String) {
        paymentWalletRepository.deletePaymentCard(paymentCardId, deleteRequest, _deleteError)
    }

    fun fetchDismissedCards() {
        loyaltyWalletRepository.retrieveDismissedCards(dismissedCardData, _fetchError)
    }

    fun addPlanIdAsDismissed(id: String) {
        loyaltyWalletRepository.addBannerAsDismissed(id, _addError)
    }
}
