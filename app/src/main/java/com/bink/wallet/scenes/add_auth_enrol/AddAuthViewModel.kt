package com.bink.wallet.scenes.add_auth_enrol

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bink.wallet.BaseViewModel
import com.bink.wallet.model.request.membership_card.MembershipCardRequest
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.model.response.payment_card.PaymentCard
import com.bink.wallet.scenes.loyalty_wallet.LoyaltyWalletRepository
import kotlin.Exception

class AddAuthViewModel constructor(private val loyaltyWalletRepository: LoyaltyWalletRepository) :
    BaseViewModel() {

    val newMembershipCard = MutableLiveData<MembershipCard>()
    val createCardError = MutableLiveData<Exception>()
    val paymentCards = MutableLiveData<List<PaymentCard>>()
    private val _fetchCardsError = MutableLiveData<Throwable>()
    val fetchCardsError: LiveData<Throwable>
        get() = _fetchCardsError
    val currentMembershipPlan = MutableLiveData<MembershipPlan>()


    fun createMembershipCard(membershipCardRequest: MembershipCardRequest) {
        loyaltyWalletRepository.createMembershipCard(
            membershipCardRequest,
            newMembershipCard,
            createCardError
        )
    }

    fun updateMembershipCard(
        membershipCardId: String,
        membershipCardRequest: MembershipCardRequest
    ) {
        loyaltyWalletRepository.updateMembershipCard(
            membershipCardId,
            membershipCardRequest,
            newMembershipCard,
            createCardError
        )
    }

    fun ghostMembershipCard(
        membershipCard: MembershipCard,
        membershipCardRequest: MembershipCardRequest
    ) {
        loyaltyWalletRepository.ghostMembershipCard(
            membershipCard.id,
            membershipCardRequest,
            newMembershipCard,
            createCardError
        )
    }

    fun getPaymentCards() {
        loyaltyWalletRepository.getPaymentCards(paymentCards, _fetchCardsError)
    }
}
