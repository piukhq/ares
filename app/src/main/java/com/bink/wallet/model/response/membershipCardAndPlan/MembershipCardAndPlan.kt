package com.bink.wallet.model.response.membershipCardAndPlan

import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan

data class MembershipCardAndPlan(
    val membershipCards: List<MembershipCard>?,
    val membershipPlans: List<MembershipPlan>?
) {
}