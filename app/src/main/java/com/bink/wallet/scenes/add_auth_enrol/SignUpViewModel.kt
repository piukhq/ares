package com.bink.wallet.scenes.add_auth_enrol

import androidx.lifecycle.MutableLiveData
import com.bink.wallet.BaseViewModel
import com.bink.wallet.model.request.membership_card.MembershipCardRequest
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.scenes.loyalty_wallet.LoyaltyWalletRepository

class SignUpViewModel constructor(private val loyaltyWalletRepository: LoyaltyWalletRepository) :
    BaseViewModel() {

    var membershipCardData: MutableLiveData<MembershipCard> = MutableLiveData()
    val createCardError = MutableLiveData<String>()

    fun createMembershipCard(membershipCardRequest: MembershipCardRequest) {
        loyaltyWalletRepository.createMembershipCard(
            membershipCardRequest,
            membershipCardData,
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
            membershipCardData,
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
