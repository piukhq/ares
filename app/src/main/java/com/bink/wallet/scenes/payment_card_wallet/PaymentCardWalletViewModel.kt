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
    val fetchError: LiveData<Throwable> get() = _fetchError
    private val _deleteError = MutableLiveData<Throwable>()
    val deleteError: LiveData<Throwable> get() = _deleteError
    private val _deleteCardError = MutableLiveData<Throwable>()
    val deleteCardError: LiveData<Throwable> get() = _deleteCardError
    private val _addError = MutableLiveData<Throwable>()
    val addError: LiveData<Throwable> get() = _addError
    val loyaltyUpdateDone = MutableLiveData<Boolean>()
    val paymentUpdateDone = MutableLiveData<Boolean>()

    fun deleteCard(id: String?) {
        loyaltyWalletRepository.deleteMembershipCard(
            id,
            deleteCard,
            _deleteCardError
        )
    }

    fun getPaymentCards() {
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

    fun deletePaymentCard(paymentCardId: String) {
        paymentWalletRepository.deletePaymentCard(paymentCardId, deleteRequest, _deleteError)
    }

    fun addPlanIdAsDismissed(id: String) {
        loyaltyWalletRepository.addBannerAsDismissed(id, _addError)
    }

    fun fetchData() {
        loyaltyWalletRepository.getLocalData(
            localMembershipPlanData,
            localMembershipCardData,
            dismissedCardData,
            _fetchError,
            loyaltyUpdateDone
        )
        paymentWalletRepository.getLocalData(paymentCards, _fetchError, paymentUpdateDone)
    }
}
