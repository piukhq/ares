package com.bink.wallet.utils

import com.bink.wallet.model.response.membership_plan.MembershipPlan
import java.util.Locale
import kotlin.Comparator

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

fun MembershipPlan.comparePlans(
    membershipPlan2: MembershipPlan
): Int {
    this.getCardType()?.type?.let { type1 ->
        membershipPlan2.getCardType()?.type?.let { type2 ->
            return when {
                (this.isPlanPLL() ||
                        membershipPlan2.isPlanPLL()) &&
                        (type1 > type2) -> -1
                (this.isPlanPLL() ||
                        membershipPlan2.isPlanPLL()) &&
                        (type1 < type2) -> 1
                else -> 0
            }
        }
    }
    return 0
}