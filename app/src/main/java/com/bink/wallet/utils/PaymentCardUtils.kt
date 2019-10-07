package com.bink.wallet.utils

import com.bink.wallet.model.payment_card.PaymentCardType
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.payment_card.PaymentCard

fun PaymentCard.isLinkedToMembershipCard(membershipCard: MembershipCard) : Boolean {
    membership_cards?.forEach { paymentMembershipCard ->
        if (paymentMembershipCard.id.toString() == membershipCard.id &&
            paymentMembershipCard.active_link == true) {
            return true
        }
    }
    return false
}

fun String.presentedCardType(): PaymentCardType {
    val sanitizedInput = this.ccSanitize()
    if (sanitizedInput.length >= 2 && listOf("34", "37").contains(sanitizedInput.substring(0, 2)))
        return PaymentCardType.AMEX
    else if (sanitizedInput.isNotEmpty() && sanitizedInput.substring(0, 1) == "4")
        return PaymentCardType.VISA
    else if (sanitizedInput.isNotEmpty() && sanitizedInput.substring(0, 1) == "5")
        return PaymentCardType.MASTERCARD
    return PaymentCardType.NONE
}

fun String.cardValidation() : PaymentCardType {
    if (!this.luhnValidation())
        return PaymentCardType.NONE
    val sanitizedInput = this.ccSanitize()
    if (sanitizedInput.length == PaymentCardType.AMEX.len &&
       listOf("34", "37").contains(sanitizedInput.substring(0, 2)))
        return PaymentCardType.AMEX
    else if (sanitizedInput.length == PaymentCardType.VISA.len &&
             sanitizedInput.substring(0, 1) == "4") // Visa
        return PaymentCardType.VISA
    else if (sanitizedInput.length == PaymentCardType.VISA.len &&
             sanitizedInput.substring(0, 1) == "5") // MasterCard
        return PaymentCardType.MASTERCARD
    else
        return PaymentCardType.NONE
}

fun String.ccSanitize(): String {
    return this.replace(" ", "")
}

fun String.luhnValidation() : Boolean {
    val sanitizedInput = this.ccSanitize()
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

fun String.cardFormatter() : String {
    val userInput = this.replace("[^\\d]".toRegex(), "")
    if (userInput.length <= 16) {
        return userInput.convertLayout()
    } else {
        return userInput
    }
}

fun String.convertLayout() : String {
    val userInput = this.ccSanitize()
    val sb = StringBuilder()
    for (i in userInput.indices) {
        if (i % 4 == 0 && i > 0) {
            sb.append(" ")
        }
        sb.append(userInput[i])
    }
    return sb.toString()
}

fun String.cardStarFormatter() : String {
    val sanitizedInput = this.ccSanitize()
    val shortPartLen = sanitizedInput.presentedCardType().len - 4
    var outPrep = ""
    if (sanitizedInput.isEmpty()) {
        outPrep = ""
    } else if (sanitizedInput.length <= shortPartLen) {
        outPrep = sanitizedInput.starMeUp()
    } else {
        val part1 = sanitizedInput.substring(0, shortPartLen)
        val part2 = sanitizedInput.substring(shortPartLen)
        val part1a = part1.starMeUp()
        outPrep = part1a + part2
    }
    val output = outPrep.convertLayout()
    return output
}

fun String.starMeUp() : String {
    return this.replace("[\\d]".toRegex(), "*")
}