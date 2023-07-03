package com.bink.wallet.scenes.browse_brands

import com.bink.wallet.data.MembershipCardDao
import com.bink.wallet.data.MembershipPlanDao

class BrowseBrandsRepository(
    private val membershipCardDao: MembershipCardDao,
    private val membershipPlanDao: MembershipPlanDao,
) {

    suspend fun getAllMembershipCards() = membershipCardDao.getAllAsync()

    suspend fun getAllMembershipPlans() = membershipPlanDao.getAllAsync()

}