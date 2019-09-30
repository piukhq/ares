package com.bink.wallet.utils

import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.payment_card.PaymentCard
import com.bink.wallet.model.response.payment_card.PaymentMembershipCard

fun PaymentCard.isLinkedToMembershipCard(membershipCard: MembershipCard) : Boolean {
    membership_cards?.forEach { paymentMembershipCard ->
        if(paymentMembershipCard.id.toString() == membershipCard.id && paymentMembershipCard.active_link == true) {
            return true
        }
    }
    return false
}