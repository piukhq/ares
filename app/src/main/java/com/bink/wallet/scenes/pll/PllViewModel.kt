package com.bink.wallet.scenes.pll

import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.bink.wallet.BaseViewModel
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.model.response.payment_card.PaymentCard

class PllViewModel(private val pllRepository: PllRepository): BaseViewModel() {
    var membershipCard = MutableLiveData<MembershipCard>()
    var membershipPlan = MutableLiveData<MembershipPlan>()
    var paymentCards = MutableLiveData<List<PaymentCard>>()
    var title = ObservableField<String>()
    var linkedPaymentCard = MutableLiveData<PaymentCard>()
    var linkError = MutableLiveData<Throwable>()
    var unlinkError = MutableLiveData<Throwable>()

    suspend fun getPaymentCards(){
        pllRepository.getPaymentCards(paymentCards)
    }

    suspend fun linkPaymentCard(cardId: String, paymentCardId: String) {
        pllRepository.linkPaymentCard(cardId, paymentCardId, linkedPaymentCard, linkError)
    }

    suspend fun unlinkPaymentCard(paymentCardId: String, cardId: String) {
        pllRepository.unlinkPaymentCard(paymentCardId, cardId, unlinkError)
    }
}