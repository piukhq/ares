package com.bink.wallet.scenes.add_payment_card

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.bink.wallet.BaseViewModel
import com.bink.wallet.modal.card_terms_and_conditions.AddPaymentCardRepository
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.model.response.payment_card.PaymentCard
import com.bink.wallet.utils.cardValidation
import com.bink.wallet.utils.combineNonNull
import com.bink.wallet.utils.dateValidation
import com.bink.wallet.utils.enums.PaymentCardType

class AddPaymentCardViewModel(private val repository: AddPaymentCardRepository) :
    BaseViewModel() {

    private val localMembershipCardData = MutableLiveData<List<MembershipCard>>()
    private val localMembershipPlanData = MutableLiveData<List<MembershipPlan>>()
    val cardNumber = MutableLiveData<String>()
    val expiryDate = MutableLiveData<String>()
    val cardHolder = MutableLiveData<String>()
    val paymentCard = MutableLiveData<PaymentCard>()
    val isAddButtonEnabled = MediatorLiveData<Boolean>()

    init {
        isAddButtonEnabled.combineNonNull(
            cardNumber,
            expiryDate,
            cardHolder,
            ::validateCardCredentials
        )
    }

    private fun validateCardCredentials(
        cardNumber: String,
        expiryDate: String,
        cardHolder: String
    ): Boolean = cardNumber.cardValidation() != PaymentCardType.NONE &&
            expiryDate.dateValidation() &&
            cardHolder.isNotEmpty()


    fun fetchLocalMembershipCards() {
        repository.retrieveStoredMembershipCards(localMembershipCardData)
    }

    fun fetchLocalMembershipPlans() {
        repository.retrieveStoredMembershipPlans(localMembershipPlanData)
    }
}
