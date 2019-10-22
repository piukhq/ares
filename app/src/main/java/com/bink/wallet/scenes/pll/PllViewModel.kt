package com.bink.wallet.scenes.pll

import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.bink.wallet.BaseViewModel
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.model.response.payment_card.PaymentCard
import okhttp3.ResponseBody

class PllViewModel(private val pllRepository: PllRepository) : BaseViewModel() {
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
    val paymentCardsLoadedCount = MutableLiveData<Int>()

    suspend fun getPaymentCards() {
        pllRepository.getPaymentCards(
            paymentCards,
            fetchError,
            paymentCardsLoadedCount
        )
    }

    suspend fun linkPaymentCard(cardId: String, paymentCardId: String) {
        pllRepository.linkPaymentCard(
            cardId,
            paymentCardId,
            linkedPaymentCard,
            linkError
        )
    }

    suspend fun unlinkPaymentCard(paymentCardId: String, cardId: String) {
        pllRepository.unlinkPaymentCard(
            paymentCardId,
            cardId,
            unlinkError,
            unlinkedRequestBody
        )
    }

    suspend fun getLocalPaymentCards() {
        pllRepository.getLocalPaymentCards(
            localPaymentCards,
            localFetchError,
            paymentCardsLoadedCount
        )
    }
}