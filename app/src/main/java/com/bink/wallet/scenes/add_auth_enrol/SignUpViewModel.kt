package com.bink.wallet.scenes.add_auth_enrol

import androidx.lifecycle.MutableLiveData
import com.bink.wallet.BaseViewModel
import com.bink.wallet.model.request.membership_card.MembershipCardRequest
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.scenes.loyalty_wallet.LoyaltyWalletRepository

class SignUpViewModel constructor(private val loyaltyWalletRepository: LoyaltyWalletRepository) :
    BaseViewModel() {

    var newMembershipCard: MutableLiveData<MembershipCard> = MutableLiveData()
    val createCardError = MutableLiveData<String>()
    var currentMembershipPlan = MutableLiveData<MembershipPlan>()
    var currentMembershipCard = MutableLiveData<MembershipCard>()

    fun createMembershipCard(membershipCardRequest: MembershipCardRequest) {
        loyaltyWalletRepository.createMembershipCard(
            membershipCardRequest,
            newMembershipCard,
            createCardError
        )
    }

    fun updateMembershipCard(
        membershipCard: MembershipCard,
        membershipCardRequest: MembershipCardRequest
    ) {
        loyaltyWalletRepository.updateMembershipCard(
            membershipCard.id,
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
            membershipCardData,
            createCardError
        )
    }
}
