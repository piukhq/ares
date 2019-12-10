package com.bink.wallet

import androidx.lifecycle.MutableLiveData
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.scenes.loyalty_wallet.LoyaltyWalletRepository
import com.bink.wallet.scenes.pll.PllViewModel

class MainViewModel(
    var loyaltyWalletRepository: LoyaltyWalletRepository,
    var pllViewModel: PllViewModel
) :
    BaseViewModel() {

    val membershipCardData = MutableLiveData<List<MembershipCard>>()
    val membershipPlanData = MutableLiveData<List<MembershipPlan>>()

    fun fetchMembershipCards() {
        loyaltyWalletRepository.retrieveMembershipCards(membershipCardData)
    }

    fun fetchMembershipPlans() {
        loyaltyWalletRepository.retrieveMembershipPlans(membershipPlanData)
    }

}
