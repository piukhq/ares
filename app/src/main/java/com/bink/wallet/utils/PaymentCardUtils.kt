package com.bink.wallet.utils

import com.bink.wallet.model.payment_card.PaymentCardType
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.payment_card.PaymentCard
import java.util.*

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
    val sanitizedInput = numberSanitize()
    if (sanitizedInput.isEmpty())
        return PaymentCardType.NONE
    PaymentCardType.values().forEach {
        if (it != PaymentCardType.NONE) {
            if (it.len >= sanitizedInput.length) {
                val splits = it.prefix.split("|")
                for (prefix in splits) {
                    // if it's not an indicator prefix, work on it
                    if (splits.size <= 1 ||
                        prefix.length != 1 ||
                        sanitizedInput.length <= prefix.length
                    ) {
                        if (sanitizedInput.length >= prefix.length &&
                            sanitizedInput.substring(0, prefix.length) == prefix
                        ) {
                            return it
                        }
                    }
                }
            }
        }
    }
    return PaymentCardType.NONE
}

fun String.cardValidation() : PaymentCardType {
    if (!luhnValidation())
        return PaymentCardType.NONE
    val sanitizedInput = ccSanitize()
    val paymentType = sanitizedInput.presentedCardType()
    return if (sanitizedInput.length == paymentType.len &&
               sanitizedInput.luhnValidation())
        paymentType
    else
        PaymentCardType.NONE
}

fun String.numberSanitize(): String {
    return this.replace("[^\\d]".toRegex(), "")
}
fun String.ccSanitize(): String {
    return replace(" ", "")
}

fun String.luhnValidation() : Boolean {
    val sanitizedInput = ccSanitize()
    return when {
        sanitizedInput != numberSanitize() -> false
        sanitizedInput.luhnLengthInvalid() -> false
        sanitizedInput.luhnValidPopulated() -> sanitizedInput.luhnChecksum() % 10 == 0
        else -> false
    }
}

fun String.luhnValidPopulated() = all(Char::isDigit) && length > 1

fun String.luhnLengthInvalid() = !(length == 16 || length == 15)

fun String.luhnChecksum() = luhnMultiply().sum()

fun String.luhnMultiply() = digits().mapIndexed { i, j ->
    when {
        (length - i + 1) % 2 == 0 -> j
        j >= 5 -> j * 2 - 9
        else -> j * 2
    }
}

fun String.digits() = map(Character::getNumericValue)

fun String.cardFormatter() : String {
    val userInput = numberSanitize()
    return if (userInput.length <= 16) {
        userInput.convertLayout()
    } else {
        userInput
    }
}

fun String.convertLayout() : String {
    val userInput = ccSanitize()
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
    val userInput = ccSanitize()
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
    val sanitizedInput = ccSanitize()
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
    val part1 = substring(0, shortPartLen)
    val part2 = substring(shortPartLen)
    val part1a = part1.starMeUp()
    return part1a + part2
}

fun String.starMeUp() : String {
    return replace("[\\d]".toRegex(), "*")
}

fun String.dateValidation(): Boolean {
    val new = formatDate()
    if (new.isNotEmpty()) {
        val split = new.split("/")
        if (split.size > 1) {
            val month = split[0].toInt()
            val year = split[1].toInt()
            if (month < 1 || month > 12)
                return false
            val cal = Calendar.getInstance()
            // presuming that a card can't expire more than 10 years in the future
            // the average expiry is about 3 years, but giving more in case
            if (year < cal.get(Calendar.YEAR) ||
                year > cal.get(Calendar.YEAR) + 10) {
                return false
            } else if (year == cal.get(Calendar.YEAR) &&
                       month < cal.get(Calendar.MONTH)) {
                return false
            }
            return true
        }
    }
    return false
}

fun String.formatDate(): String {
    val builder = StringBuilder()
    val new = replace("[^\\d/]".toRegex(), "")
    if (new.isNotEmpty()) {
        val parts = new.split("/")
        var year = ""
        var month = ""
        if (parts.size == 1) {
            val len = Math.max(0, length - 2)
            month = new.substring(0, len)
            year = new.substring(len)
        } else {
            month = parts[0]
            year = parts[1]
        }
        month = "00$month"
        builder.append(month.substring(month.length - 2))
        builder.append("/")
        builder.append(year)
    }
    return builder.toString()
}