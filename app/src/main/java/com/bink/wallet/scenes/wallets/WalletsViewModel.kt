package com.bink.wallet.scenes.wallets

import androidx.lifecycle.MutableLiveData
import com.bink.wallet.BaseViewModel
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.scenes.loyalty_wallet.LoyaltyWalletRepository

class WalletsViewModel(private var repository: LoyaltyWalletRepository) : BaseViewModel() {

    var membershipPlanData: MutableLiveData<List<MembershipPlan>> = MutableLiveData()


    suspend fun fetchMembershipPlans() {
        repository.retrieveMembershipPlans(membershipPlanData)
    }

}