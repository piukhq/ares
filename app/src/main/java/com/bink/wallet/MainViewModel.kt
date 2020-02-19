package com.bink.wallet

import androidx.lifecycle.MutableLiveData
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.scenes.loyalty_wallet.LoyaltyWalletRepository

class MainViewModel constructor(val loyaltyWalletRepository: LoyaltyWalletRepository) :
    BaseViewModel() {

    private val membershipPlanMutableLiveData: MutableLiveData<List<MembershipPlan>> =
        MutableLiveData()
    private val membershipPlanErrorLiveData: MutableLiveData<Throwable> = MutableLiveData()

    //todo test with no auth
    //todo bind loyaltywallerfragment with this viewmodel, to keep the UI up to date
    fun getMembershipPlans() {
        loyaltyWalletRepository.retrieveMembershipPlans(
            membershipPlanMutableLiveData,
            membershipPlanErrorLiveData, false
        )
    }

}