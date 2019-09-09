package com.bink.wallet.scenes.add_auth

import androidx.lifecycle.MutableLiveData
import com.bink.wallet.BaseViewModel
import com.bink.wallet.model.request.membership_card.MembershipCardRequest
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.scenes.loyalty_wallet.LoyaltyWalletRepository

class AddAuthViewModel constructor(private val loyaltyWalletRepository: LoyaltyWalletRepository) : BaseViewModel() {

    var membershipCardData: MutableLiveData<MembershipCard> = MutableLiveData()
    val createCardError = MutableLiveData<String>()

    fun createMembershipCard(membershipCardRequest: MembershipCardRequest) {
        loyaltyWalletRepository.createMembershipCard(membershipCardRequest, membershipCardData, createCardError)
    }
}
