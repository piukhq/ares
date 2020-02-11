package com.bink.wallet.scenes.add_payment_card

import androidx.lifecycle.MutableLiveData
import com.bink.wallet.BaseViewModel
import com.bink.wallet.modal.card_terms_and_conditions.AddPaymentCardRepository
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.model.response.payment_card.PaymentCard
import com.bink.wallet.model.response.payment_card.PaymentCardAdd

class AddPaymentCardViewModel(private val repository: AddPaymentCardRepository) :
    BaseViewModel() {

    val cardNumber = MutableLiveData<String>()
    val expiryDate = MutableLiveData<String>()
    val cardHolder = MutableLiveData<String>()

    var paymentCard = MutableLiveData<PaymentCard>()
    var addedError = MutableLiveData<Throwable>()

    var localMembershipCardData = MutableLiveData<List<MembershipCard>>()
    var localMembershipPlanData = MutableLiveData<List<MembershipPlan>>()

    fun sendAddCard(card: PaymentCardAdd) {
        addedError.value = null
        repository.sendAddCard(card, paymentCard, addedError)
    }

    fun fetchLocalMembershipCards() {
        repository.retrieveStoredMembershipCards(localMembershipCardData)
    }

    fun fetchLocalMembershipPlans() {
        repository.retrieveStoredMembershipPlans(localMembershipPlanData)
    }
}
