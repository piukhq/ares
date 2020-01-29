package com.bink.wallet.model.response.membership_card

import com.bink.wallet.model.response.membership_plan.MembershipPlan

sealed class UserDataResult {
    object UserDataLoading : UserDataResult()
    data class UserDataSuccess(val result: Triple<List<MembershipCard>, List<MembershipPlan>, List<Any>>) :
        UserDataResult()
}