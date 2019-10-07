package com.bink.wallet.utils

import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.payment_card.PaymentCard

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
        sanitizedInput.luhnLengthInvalid() -> false
        sanitizedInput.luhnValidPopulated() -> sanitizedInput.luhnChecksum() % 10 == 0
        else -> false
    }
}

fun String.luhnValidPopulated() = this.all(Char::isDigit) && this.length > 1

fun String.luhnLengthInvalid() = !(this.length == 16 || this.length == 15)

fun String.luhnChecksum() = this.luhnMultiply().sum()

fun String.luhnMultiply() = this.digits().mapIndexed { i, j ->
    when {
        (this.length - i + 1) % 2 == 0 -> j
        j >= 5 -> j * 2 - 9
        else -> j * 2
    }
}

fun String.digits() = this.map(Character::getNumericValue)
