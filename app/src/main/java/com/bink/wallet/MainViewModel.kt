package com.bink.wallet

import androidx.lifecycle.MutableLiveData
import com.bink.wallet.data.SharedPreferenceManager
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.scenes.loyalty_wallet.LoyaltyWalletRepository
import com.bink.wallet.utils.DateTimeUtils

class MainViewModel constructor(val loyaltyWalletRepository: LoyaltyWalletRepository) :
    BaseViewModel() {

    private val membershipPlanMutableLiveData: MutableLiveData<List<MembershipPlan>> =
        MutableLiveData()
    private val membershipPlanErrorLiveData: MutableLiveData<Throwable> = MutableLiveData()
    val membershipPlanDatabaseLiveData =
        loyaltyWalletRepository.getMembershipPlansDatabaseNotifier()

    fun getMembershipPlans() {
        val wasAnHourAgo =
            DateTimeUtils.hasAnHourElapsed(SharedPreferenceManager.membershipPlansLastRequestTime)
        if (wasAnHourAgo) {
            loyaltyWalletRepository.retrieveMembershipPlans(
                membershipPlanMutableLiveData,
                membershipPlanErrorLiveData,
                false
            )
        }
    }

}