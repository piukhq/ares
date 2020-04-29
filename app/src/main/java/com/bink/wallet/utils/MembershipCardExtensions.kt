package com.bink.wallet.utils

import com.bink.wallet.model.response.membership_card.MembershipCard

fun List<MembershipCard>.getOwnedMembershipCardsIds(): List<String> {
    val membershipCardsIds = mutableListOf<String>()
    this.forEach { membershipCard ->
        membershipCard.membership_plan?.let { membership_planId ->
            if (membership_planId !in membershipCardsIds) {
                membershipCardsIds.add(membership_planId)
            }
        }
    }
    return membershipCardsIds
}