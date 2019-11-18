package com.bink.wallet.utils

import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.utils.enums.CardCodes
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
                    return LoginStatus.STATUS_PENDING
                }

                FAILED.status -> {
                    if (membershipCard.status?.reason_codes?.intersect(listOf(CardCodes.X201.code)) != null) {
                        return LoginStatus.STATUS_SIGN_UP_FAILED
                    }
                    if (membershipCard.status?.reason_codes?.intersect(listOf(CardCodes.X202.code)) != null) {
                        return LoginStatus.STATUS_CARD_ALREADY_EXISTS
                    }
                    if (membershipCard.status?.reason_codes?.intersect(
                            listOf(
                                CardCodes.X101.code,
                                CardCodes.X102.code,
                                CardCodes.X103.code,
                                CardCodes.X104.code,
                                CardCodes.X302.code,
                                CardCodes.X303.code,
                                CardCodes.X304.code
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