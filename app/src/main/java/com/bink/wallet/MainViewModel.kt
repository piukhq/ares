package com.bink.wallet

import androidx.lifecycle.MutableLiveData
import com.bink.wallet.data.SharedPreferenceManager
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.scenes.loyalty_wallet.LoyaltyWalletRepository
import com.bink.wallet.utils.DateTimeUtils

class MainViewModel constructor(val loyaltyWalletRepository: LoyaltyWalletRepository) :
    BaseViewModel() {

    private val membershipPlanData: MutableLiveData<List<MembershipPlan>> =
        MutableLiveData()
    private val membershipPlanError: MutableLiveData<Throwable> = MutableLiveData()
    val membershipPlanDatabaseLiveData: MutableLiveData<Boolean> = MutableLiveData()

    fun getMembershipPlans() {
        val wasAnHourAgo =
            DateTimeUtils.hasAnHourElapsed(SharedPreferenceManager.membershipPlansLastRequestTime)
        if (wasAnHourAgo && SharedPreferenceManager.isUserLoggedIn) {
            loyaltyWalletRepository.retrieveMembershipPlans(
                membershipPlanData,
                membershipPlanError,
                membershipPlanDatabaseLiveData
            )
        }
    }

}