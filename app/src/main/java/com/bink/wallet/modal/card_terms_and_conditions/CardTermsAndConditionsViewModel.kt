package com.bink.wallet.modal.card_terms_and_conditions

import androidx.lifecycle.MutableLiveData
import com.bink.wallet.modal.generic.BaseModalViewModel
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.model.response.payment_card.PaymentCard
import com.bink.wallet.model.response.payment_card.PaymentCardAdd

class CardTermsAndConditionsViewModel(private val repository: AddPaymentCardRepository) :
    BaseModalViewModel() {
    val paymentCard = MutableLiveData<PaymentCard>()
    val error = MutableLiveData<Throwable>()
    var localMembershipPlanData = MutableLiveData<List<MembershipPlan>>()
    var localMembershipCardData = MutableLiveData<List<MembershipCard>>()

    fun sendAddCard(card: PaymentCardAdd, cardNumber: String) {
        error.value = null
        repository.sendAddCard(card, cardNumber, paymentCard, error)
    }

    fun fetchLocalMembershipCards() {
        repository.retrieveStoredMembershipCards(localMembershipCardData)
    }

    fun fetchLocalMembershipPlans() {
        repository.retrieveStoredMembershipPlans(localMembershipPlanData)
    }

}