package com.bink.wallet.utils

import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.model.response.payment_card.PaymentCard
import com.bink.wallet.utils.enums.CardCodes
import com.bink.wallet.utils.enums.CardType
import com.bink.wallet.utils.enums.LinkStatus
import com.bink.wallet.utils.enums.LoginStatus
import com.bink.wallet.utils.enums.MembershipCardStatus.*

object MembershipPlanUtils {

    fun getAccountStatus(
        membershipPlan: MembershipPlan,
        membershipCard: MembershipCard
    ): LoginStatus {
        if (membershipPlan.feature_set?.has_points == true ||
            membershipPlan.feature_set?.transactions_available == true
        ) {
            when (membershipCard.status?.state) {
                AUTHORISED.status -> {
                    return if (membershipCard.balances.isNullOrEmpty()) {
                        LoginStatus.STATUS_PENDING
                    } else {
                        if (membershipPlan.feature_set.transactions_available == true && membershipPlan.feature_set.has_vouchers == true) {
                            LoginStatus.STATUS_LOGGED_IN_HISTORY_AND_VOUCHERS_AVAILABLE
                        } else if (membershipPlan.feature_set.transactions_available == true) {
                            LoginStatus.STATUS_LOGGED_IN_HISTORY_AVAILABLE
                        } else {
                            LoginStatus.STATUS_LOGGED_IN_HISTORY_UNAVAILABLE
                        }
                    }
                }

                PENDING.status -> {
                    return LoginStatus.STATUS_PENDING
                }

                FAILED.status,
                UNAUTHORISED.status -> {
                    membershipCard.status?.reason_codes?.let { reasonCodes ->
                        if (!reasonCodes.intersect(listOf(CardCodes.X201.code)).isNullOrEmpty()) {
                            return LoginStatus.STATUS_SIGN_UP_FAILED
                        }
                        if (!reasonCodes.intersect(listOf(CardCodes.X202.code)).isNullOrEmpty()) {
                            return LoginStatus.STATUS_CARD_ALREADY_EXISTS
                        }
                        if (!reasonCodes.intersect(
                                listOf(
                                    CardCodes.X101.code,
                                    CardCodes.X102.code,
                                    CardCodes.X103.code,
                                    CardCodes.X104.code,
                                    CardCodes.X302.code,
                                    CardCodes.X303.code,
                                    CardCodes.X304.code
                                )
                            ).isNullOrEmpty()
                        ) {
                            return LoginStatus.STATUS_LOGIN_FAILED
                        }
                        if (!reasonCodes.intersect(listOf(CardCodes.X105.code)).isNullOrEmpty()) {
                            return LoginStatus.STATUS_REGISTRATION_REQUIRED_GHOST_CARD
                        }
                        if (reasonCodes.isNullOrEmpty()) {
                            return LoginStatus.STATUS_NO_REASON_CODES
                        }
                    }
                }
            }
        } else {
            return LoginStatus.STATUS_LOGIN_UNAVAILABLE
        }
        return LoginStatus.STATUS_LOGIN_UNAVAILABLE
    }

    fun getLinkStatus(
        membershipPlan: MembershipPlan,
        membershipCard: MembershipCard,
        paymentCards: MutableList<PaymentCard>
    ): LinkStatus {
        when (membershipPlan.feature_set?.card_type) {
            CardType.PLL.type -> {
                when (membershipCard.status?.state) {
                    AUTHORISED.status -> {
                        return when {
                            paymentCards.isNullOrEmpty() || hasNoActiveCards(paymentCards) -> {
                                LinkStatus.STATUS_LINKABLE_NO_PAYMENT_CARDS
                            }
                            membershipCard.payment_cards.isNullOrEmpty() ||
                                    !existLinkedPaymentCards(membershipCard, paymentCards) -> {
                                LinkStatus.STATUS_LINKABLE_NO_PAYMENT_CARDS_LINKED
                            }
                            else -> {
                                LinkStatus.STATUS_LINKED_TO_SOME_OR_ALL
                            }
                        }
                    }
                    UNAUTHORISED.status -> {
                        return LinkStatus.STATUS_LINKABLE_REQUIRES_AUTH
                    }
                    PENDING.status -> {
                        return LinkStatus.STATUS_LINKABLE_REQUIRES_AUTH_PENDING
                    }
                    FAILED.status -> {
                        var isGhostCard = false
                        var isFailedSignUp = false

                        membershipCard.status?.reason_codes?.forEach { reasonCode ->
                            isGhostCard = reasonCode == CardCodes.X105.name
                            isFailedSignUp = reasonCode == CardCodes.X201.name
                        }

                        return if (membershipCard.status?.reason_codes.isNullOrEmpty()) {
                            LinkStatus.STATUS_NO_REASON_CODES
                        } else if (isGhostCard) {
                            LinkStatus.STATUS_LINKABLE_REQUIRES_AUTH_GHOST_CARD
                        } else if (isFailedSignUp) {
                            LinkStatus.STATUS_LINKABLE_SIGN_UP_FAILED
                        } else {
                            LinkStatus.STATUS_LINKABLE_REQUIRES_AUTH_PENDING_FAILED
                        }
                    }
                }
            }
            CardType.VIEW.type,
            CardType.STORE.type -> {
                return LinkStatus.STATUS_UNLINKABLE
            }
        }
        return LinkStatus.STATUS_UNLINKABLE
    }

    fun existLinkedPaymentCards(
        membershipCard: MembershipCard,
        paymentCards: MutableList<PaymentCard>
    ): Boolean {
        countLinkedPaymentCards(membershipCard, paymentCards)?.let {
            return it > 0
        }
        return false
    }

    fun countLinkedPaymentCards(
        membershipCard: MembershipCard,
        paymentCards: MutableList<PaymentCard>
    ): Int? {
        val paymentCardIds = mutableListOf<String>()
        paymentCards.forEach { paymentCard ->
            paymentCardIds.add(paymentCard.id.toString())
        }
        return membershipCard.payment_cards?.count { card ->
            paymentCardIds.contains(card.id) && card.active_link == true
        }
    }

    private fun hasNoActiveCards(paymentCards: List<PaymentCard>): Boolean {
        val originalSize = paymentCards.size
        val filteredCards = paymentCards.filter { card -> card.status == PAYMENT_CARD_STATUS_PENDING }

        return originalSize == filteredCards.size


    }
}