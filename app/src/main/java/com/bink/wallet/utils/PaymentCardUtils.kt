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
    if (sanitizedInput.isEmpty())
        return PaymentCardType.NONE
    PaymentCardType.values().forEach {
        if (it != PaymentCardType.NONE) {
            val splits = it.prefix.split("|")
            for (prefix in splits) {
                if (splits.size <= 1 ||
                    prefix.length != 1 ||
                    sanitizedInput.length <= prefix.length) {
                    if (sanitizedInput.length >= prefix.length &&
                        sanitizedInput.substring(0, prefix.length) == prefix
                    ) {
                        return it
                    }
                }
                // skip this, as it's an indicator prefix
            }
        }
    }
    return PaymentCardType.NONE
}

fun String.cardValidation() : PaymentCardType {
    if (!this.luhnValidation())
        return PaymentCardType.NONE
    val sanitizedInput = this.ccSanitize()
    val paymentType = sanitizedInput.presentedCardType()
    return if (sanitizedInput.length == paymentType.len)
        paymentType
    else
        PaymentCardType.NONE
}

fun String.numberSanitize(): String {
    return this.replace("[^\\d]".toRegex(), "")
}
fun String.ccSanitize(): String {
    return this.replace(" ", "")
}

fun String.luhnValidation() : Boolean {
    val sanitizedInput = this.ccSanitize()
    return when {
        sanitizedInput != this.numberSanitize() -> false
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
    val userInput = this.numberSanitize()
    return if (userInput.length <= 16) {
        userInput.convertLayout()
    } else {
        userInput
    }
}

fun String.convertLayout() : String {
    val userInput = this.ccSanitize()
    val sb = StringBuilder()
    val type = userInput.presentedCardType()
    if (type != PaymentCardType.NONE) {
        val layout = type.format
        var pos = 0
        for (i in layout.indices) {
            if (pos >= userInput.length) {
                break
            } else if (layout[i].toString() == " ") {
                sb.append(layout[i])
            } else {
                sb.append(userInput[pos++])
            }
        }
    }
    return sb.toString()
}
fun String.fourBlockLayout() : String {
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
    val type = sanitizedInput.presentedCardType()
    if (type == PaymentCardType.NONE) {
        return ""
    }
    val shortPartLen = type.len - 4
    val outPrep = if (sanitizedInput.isEmpty()) {
        ""
    } else if (sanitizedInput.length <= shortPartLen) {
        sanitizedInput.starMeUp()
    } else {
        if (type.len == 15 && sanitizedInput.length > type.len - 4) {
            "0$sanitizedInput".cardStarConcatenator(shortPartLen + 1)
        } else {
            sanitizedInput.cardStarConcatenator(shortPartLen)
        }
    }
    return outPrep.fourBlockLayout()
}

fun String.cardStarConcatenator(shortPartLen: Int): String {
    val part1 = this.substring(0, shortPartLen)
    val part2 = this.substring(shortPartLen)
    val part1a = part1.starMeUp()
    return part1a + part2
}

fun String.starMeUp() : String {
    return this.replace("[\\d]".toRegex(), "*")
}