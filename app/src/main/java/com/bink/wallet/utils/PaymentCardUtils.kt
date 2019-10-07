package com.bink.wallet.utils

import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.payment_card.PaymentCard
import com.bink.wallet.model.response.payment_card.PaymentMembershipCard

fun PaymentCard.isLinkedToMembershipCard(membershipCard: MembershipCard) : Boolean {
    membership_cards?.forEach { paymentMembershipCard ->
        if(paymentMembershipCard.id.toString() == membershipCard.id
            && paymentMembershipCard.active_link == true) {
            return true
        }
    }
    return false
}

fun String.luhnValidation() : Boolean {
    val sanitizedInput = this.replace(" ", "")
    return when {
        sanitizedInput.luhnValidPopulated() -> sanitizedInput.checksum() % 10 == 0
        else -> false
    }
}

fun String.luhnValidPopulated() = this.all(Char::isDigit) && this.length > 1

fun String.checksum() = this.addends().sum()

fun String.addends() = this.digits().mapIndexed { i, j ->
    when {
        (this.length - i + 1) % 2 == 0 -> j
        j >= 5 -> j * 2 - 9
        else -> j * 2
    }
}

fun String.digits() = this.map(Character::getNumericValue)
