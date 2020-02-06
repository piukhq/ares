package com.bink.wallet.scenes.pll

import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bink.wallet.BaseViewModel
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.model.response.payment_card.PaymentCard
import okhttp3.ResponseBody

class PllViewModel(private val paymentWalletRepository: PaymentWalletRepository) : BaseViewModel() {
    val membershipCard = MutableLiveData<MembershipCard>()
    val membershipPlan = MutableLiveData<MembershipPlan>()
    val title = ObservableField<String>()
    val unlinkedRequestBody = MutableLiveData<ResponseBody>()
    val linkError = MutableLiveData<Throwable>()
    val unlinkError = MutableLiveData<Throwable>()
    val fetchError = MutableLiveData<Throwable>()
    val localFetchError = MutableLiveData<Throwable>()

    val paymentCard = MutableLiveData<PaymentCard>()

    private val _paymentCards = MutableLiveData<List<PaymentCard>>()
    val paymentCards: LiveData<List<PaymentCard>>
        get() = _paymentCards

    private val _localPaymentCards = MutableLiveData<List<PaymentCard>>()
    val localPaymentCards: LiveData<List<PaymentCard>>
        get() = _localPaymentCards

    fun getPaymentCards() {
        paymentWalletRepository.getPaymentCards(
            _paymentCards,
            fetchError
        )
    }

    fun linkPaymentCard(cardId: String, paymentCardId: String) {
        paymentWalletRepository.linkPaymentCard(
            cardId,
            paymentCardId,
            linkError,
            paymentCard
        )
    }

    fun unlinkPaymentCard(paymentCardId: String, cardId: String) {
        paymentWalletRepository.unlinkPaymentCard(
            paymentCardId,
            cardId,
            unlinkError,
            unlinkedRequestBody,
            paymentCard
        )
    }

    fun getLocalPaymentCards() {
        paymentWalletRepository.getLocalPaymentCards(
            _localPaymentCards,
            localFetchError
        )
    }
}