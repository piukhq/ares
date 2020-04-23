package com.bink.wallet.utils

import com.bink.wallet.model.response.membership_plan.MembershipPlan
import java.util.Locale

fun List<MembershipPlan>.sortedByCardTypeAndCompany(): List<MembershipPlan> =
    this.sortedWith(
        Comparator<MembershipPlan> { membershipPlan1, membershipPlan2 ->
            membershipPlan1.comparePlans(membershipPlan2)
        }.thenBy {
            it.account?.company_name?.toLowerCase(Locale.ENGLISH)
        }
    )

fun List<MembershipPlan>.getCategories(): List<String> {
    val categories = mutableListOf<String>()
    this.forEach { membershipPlan ->
        membershipPlan.account?.category?.let { category ->
            if (category !in categories) {
                categories.add(category)
            }
        }
    }
    return categories
}