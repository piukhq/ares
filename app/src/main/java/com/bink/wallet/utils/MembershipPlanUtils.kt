package com.bink.wallet.utils

import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.utils.enums.CardStatus.*
import com.bink.wallet.utils.enums.LoginStatus

object MembershipPlanUtils {

    fun getAccountStatus(
        membershipPlan: MembershipPlan,
        membershipCard: MembershipCard
    ): LoginStatus {
        if (membershipPlan.feature_set?.has_points == true || membershipPlan.feature_set?.transactions_available == true) {
            when (membershipCard.status?.state) {
                AUTHORISED.status -> {
                    return if (membershipPlan.feature_set.transactions_available == true) {
                        LoginStatus.STATUS_LOGGED_IN_HISTORY_AVAILABLE
                    } else {
                        LoginStatus.STATUS_LOGGED_IN_HISTORY_UNAVAILABLE
                    }
                }

                UNAUTHORISED.status -> {
                    return if (membershipPlan.feature_set.transactions_available == true) {
                        LoginStatus.STATUS_NOT_LOGGED_IN_HISTORY_AVAILABLE
                    } else {
                        LoginStatus.STATUS_NOT_LOGGED_IN_HISTORY_UNAVAILABLE
                    }
                }

                PENDING.status -> {
                    if (membershipCard.status?.reason_codes?.intersect(listOf("X200")) != null) {
                        return LoginStatus.STATUS_SIGN_UP_PENDING
                    }
                    if (membershipCard.status?.reason_codes?.intersect(
                            listOf("X100", "X301")
                        ) != null
                    ) {
                        return LoginStatus.STATUS_LOGIN_FAILED
                    }
                }

                FAILED.status -> {
                    if (membershipCard.status?.reason_codes?.intersect(listOf("X201")) != null) {
                        return LoginStatus.STATUS_SIGN_UP_FAILED
                    }
                    if (membershipCard.status?.reason_codes?.intersect(listOf("X202")) != null) {
                        return LoginStatus.STATUS_CARD_ALREADY_EXISTS
                    }
                    if (membershipCard.status?.reason_codes?.intersect(
                            listOf(
                                "X101",
                                "X102",
                                "X103",
                                "X104",
                                "X302",
                                "X303",
                                "X304"
                            )
                        ) != null
                    ) {
                        return LoginStatus.STATUS_LOGIN_FAILED
                    }
                }
            }
        } else {
            return LoginStatus.STATUS_LOGIN_UNAVAILABLE
        }
        return LoginStatus.STATUS_LOGIN_UNAVAILABLE
    }
}