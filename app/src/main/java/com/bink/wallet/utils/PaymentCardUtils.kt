package com.bink.wallet.utils

import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.payment_card.PaymentCard
import com.bink.wallet.utils.enums.PaymentCardType
import java.util.*

const val SEPARATOR_PIPE = "|"
const val SEPARATOR_HYPHEN = "-"
const val SEPARATOR_SLASH = "/"
const val REGEX_DECIMAL_ONLY = "[^\\d]"
const val REGEX_DECIMAL_OR_SLASH = "[^\\d/]"
const val DIGITS_VISA_MASTERCARD = 16
const val DIGITS_AMERICAN_EXPRESS = 15
const val PENDING_CARD = "Pending"
const val FAILED_CARD = "Failed"

fun PaymentCard.isLinkedToMembershipCard(membershipCard: MembershipCard): Boolean {
    membership_cards.forEach { paymentMembershipCard ->
        if (paymentMembershipCard.id.toString() == membershipCard.id &&
            paymentMembershipCard.active_link == true
        ) {
            return true
        }
    }
    return false
}

fun String.getCardType() =
    PaymentCardType.values().firstOrNull { it.type == this } ?: PaymentCardType.NONE

fun String.getCardTypeFromProvider() =
    PaymentCardType.values().firstOrNull { it.type == this } ?: PaymentCardType.NONE

fun String.presentedCardType(): PaymentCardType {
    val sanitizedInput = numberSanitize()
    if (sanitizedInput.isEmpty()) {
        return PaymentCardType.NONE
    }
    PaymentCardType.values().forEach {
        if (it != PaymentCardType.NONE &&
            it.len >= sanitizedInput.length
        ) {
            val splits = it.prefix.split(SEPARATOR_PIPE)
            for (prefix in splits) {
                if (splits.size <= 1 ||
                    prefix.length != 1 ||
                    sanitizedInput.length <= prefix.length
                ) {
                    val range = prefix.split(SEPARATOR_HYPHEN)
                    if (range.size > 1 &&
                        sanitizedInput >= range[0] &&
                        sanitizedInput <= range[1]
                    ) {
                        return it
                    } else if (sanitizedInput.length >= prefix.length &&
                        sanitizedInput.substring(0, prefix.length) == prefix
                    ) {
                        return it
                    }
                }
            }
        }
    }
    return PaymentCardType.NONE
}

fun String.numberSanitize() = replace(REGEX_DECIMAL_ONLY.toRegex(), EMPTY_STRING)

fun String.ccSanitize() = replace(SPACE, EMPTY_STRING)

fun String.isValidLuhnFormat(): Boolean {
    val sanitizedInput = ccSanitize()
    return when {
        sanitizedInput != numberSanitize() -> false
        sanitizedInput.luhnLengthInvalid() -> false
        sanitizedInput.luhnValidPopulated() -> sanitizedInput.luhnChecksum() % 10 == 0
        else -> false
    }
}

fun String.luhnValidPopulated() = all(Char::isDigit) && length > 1

fun String.luhnLengthInvalid() =
    !(length == DIGITS_VISA_MASTERCARD ||
            length == DIGITS_AMERICAN_EXPRESS)

fun String.luhnChecksum() = luhnMultiply().sum()

fun String.luhnMultiply() = digits().mapIndexed { i, j ->
    when {
        (length - i + 1) % 2 == 0 -> j
        j >= 5 -> j * 2 - 9
        else -> j * 2
    }
}

fun String.digits() = map(Character::getNumericValue)

fun String.cardFormatter(): String {
    val userInput = numberSanitize()
    return if (userInput.length <= 16) {
        userInput.convertLayout()
    } else {
        userInput
    }
}

fun String.convertLayout(): String {
    val userInput = ccSanitize()
    val sb = StringBuilder()
    val type = userInput.presentedCardType()
    if (type != PaymentCardType.NONE) {
        val layout = type.format
        var pos = 0
        for (i in layout.indices) {
            if (pos >= userInput.length) {
                break
            } else if (layout[i].toString() == SPACE) {
                sb.append(layout[i])
            } else {
                sb.append(userInput[pos++])
            }
        }
    }
    return sb.toString()
}

fun String.fourBlockLayout(): String {
    val userInput = ccSanitize()
    val sb = StringBuilder()
    for (i in userInput.indices) {
        if (i % 4 == 0 && i > 0) {
            sb.append(SPACE)
        }
        sb.append(userInput[i])
    }
    return sb.toString()
}

fun String.cardStarFormatter(): String {
    val sanitizedInput = ccSanitize()
    val type = sanitizedInput.presentedCardType()
    if (type == PaymentCardType.NONE) {
        return EMPTY_STRING
    }
    val shortPartLen = type.len - 4
    val outPrep = if (sanitizedInput.isEmpty()) {
        EMPTY_STRING
    } else if (sanitizedInput.length <= shortPartLen) {
        sanitizedInput.starMeUp()
    } else if (type.len == 15 &&
        sanitizedInput.length > type.len - 4
    ) {
        "0$sanitizedInput".cardStarConcatenator(shortPartLen + 1)
    } else {
        sanitizedInput.cardStarConcatenator(shortPartLen)
    }
    return outPrep.fourBlockLayout()
}

fun String.cardStarConcatenator(shortPartLen: Int): String {
    val part1 = substring(0, shortPartLen)
    val part2 = substring(shortPartLen)
    val part1a = part1.starMeUp()
    return part1a + part2
}

fun String.starMeUp() = replace("[\\d]".toRegex(), "•")

fun String.dateValidation(): Boolean {
    val new = formatDate()
    if (new.isNotEmpty()) {
        val split = new.split(SEPARATOR_SLASH)
        if (split.size > 1 &&
            split[0].isNotBlank() &&
            split[1].isNotBlank()
        ) {
            val month = split[0].toInt()
            val year = split[1].toInt() + 2000
            if (month < 1 ||
                month > 12
            ) {
                return false
            }
            val cal = Calendar.getInstance()
            // presuming that a card can't expire more than 10 years in the future
            // the average expiry is about 3 years, but giving more in case
            if (year < cal.get(Calendar.YEAR) ||
                year > cal.get(Calendar.YEAR) + 10
            ) {
                return false
            } else if (year == cal.get(Calendar.YEAR) &&
                month <= cal.get(Calendar.MONTH)
            ) {
                return false
            }
            return true
        }
    }
    return false
}

fun String.formatDate(): String {
    val builder = StringBuilder()
    try {
        val new = replace(REGEX_DECIMAL_OR_SLASH.toRegex(), EMPTY_STRING)
        if (new.isNotEmpty()) {
            val parts = new.split(SEPARATOR_SLASH)
            val year: String
            var month: String
            if (parts.size == 1) {
                val len = kotlin.math.max(0, length - 2)
                month = new.substring(0, len)
                year = new.substring(len)
            } else {
                month = parts[0]
                year = parts[1]
            }
            month = "00$month"
            builder.append(month.substring(month.length - 2))
            builder.append(SEPARATOR_SLASH)
            builder.append(year)
        }
        return builder.toString()
    } catch (e: StringIndexOutOfBoundsException) {
        return ""
    }
}

object PaymentCardUtils {

    fun existLinkedMembershipCards(
        paymentCard: PaymentCard,
        membershipCards: MutableList<MembershipCard>
    ): Boolean {
        return countLinkedPaymentCards(paymentCard, membershipCards) > 0
    }

    fun countLinkedPaymentCards(
        paymentCard: PaymentCard,
        membershipCards: MutableList<MembershipCard>
    ): Int {
        val membershipCardIds = mutableListOf<String>()
        membershipCards.forEach { membershipCard ->
            membershipCardIds.add(membershipCard.id)
        }
        return paymentCard.membership_cards.count { card ->
            membershipCardIds.contains(card.id) && card.active_link == true
        }
    }

    fun cardStatus(status: String): String {
        return when (status.lowercase(Locale.getDefault())) {
            "pending" -> PENDING_CARD
            "failed" -> FAILED_CARD
            else -> PENDING_CARD

        }
    }

    fun inDateCards(paymentCards: List<PaymentCard>): List<PaymentCard> {
        val activeCards = mutableListOf<PaymentCard>()
        paymentCards.forEach { pCard ->
            pCard.card?.let {
                if (!it.isExpired()) {
                    activeCards.add(pCard)
                }
            }
        }

        return activeCards
    }
}