package com.bink.wallet.utils

import com.bink.android_core.PaymentAccountUtil
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.payment_card.PaymentCard
import com.bink.wallet.utils.enums.PaymentCardType
import java.util.*

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

fun String.convertToPaymentCardType(): PaymentCardType? {
    return PaymentCardType.values().find { it.type == this }
}

fun String.cardFormatter(): String {
    val userInput = PaymentAccountUtil.numberSanitize(this)
    return if (userInput.length <= 16) {
        userInput.convertLayout()
    } else {
        userInput
    }
}

fun String.convertLayout(): String {
    val userInput = PaymentAccountUtil.ccSanitize(this)
    val sb = StringBuilder()
    val type = PaymentAccountUtil.presentedCardType(userInput)
    if (type.type != PaymentCardType.NONE.type) {
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
    val userInput = PaymentAccountUtil.ccSanitize(this)
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
    val sanitizedInput = PaymentAccountUtil.ccSanitize(this)
    val type = PaymentAccountUtil.presentedCardType(sanitizedInput)
    if (type.type == PaymentCardType.NONE.type) {
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

object PaymentCardUtils {

    fun existLinkedMembershipCards(
        paymentCard: PaymentCard,
        membershipCards: MutableList<MembershipCard>,
    ): Boolean {
        return countLinkedPaymentCards(paymentCard, membershipCards) > 0
    }

    fun countLinkedPaymentCards(
        paymentCard: PaymentCard,
        membershipCards: MutableList<MembershipCard>,
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