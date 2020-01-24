package com.bink.wallet.scenes.pll

import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.bink.wallet.BaseViewModel
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.model.response.payment_card.PaymentCard
import okhttp3.ResponseBody

class PllViewModel(private val paymentWalletRepository: PaymentWalletRepository) : BaseViewModel() {
    val membershipCard = MutableLiveData<MembershipCard>()
    val membershipPlan = MutableLiveData<MembershipPlan>()
    val paymentCards = MutableLiveData<List<PaymentCard>>()
    val localPaymentCards = MutableLiveData<List<PaymentCard>>()
    val title = ObservableField<String>()
    val linkedPaymentCard = MutableLiveData<PaymentCard>()
    val unlinkedRequestBody = MutableLiveData<ResponseBody>()
    val linkError = MutableLiveData<Throwable>()
    val unlinkError = MutableLiveData<Throwable>()
    val fetchError = MutableLiveData<Throwable>()
    val localFetchError = MutableLiveData<Throwable>()

    suspend fun getPaymentCards() {
        paymentWalletRepository.getPaymentCards(
            paymentCards,
            fetchError
        )
    }

    suspend fun linkPaymentCard(cardId: String, paymentCardId: String) {
        paymentWalletRepository.linkPaymentCard(
            cardId,
            paymentCardId,
            linkedPaymentCard,
            linkError
        )
    }

    suspend fun unlinkPaymentCard(paymentCardId: String, cardId: String) {
        paymentWalletRepository.unlinkPaymentCard(
            paymentCardId,
            cardId,
            unlinkError,
            unlinkedRequestBody
        )
    }

    fun getLocalPaymentCards() {
        paymentWalletRepository.getLocalPaymentCards(
            localPaymentCards,
            localFetchError
        )
    }
}