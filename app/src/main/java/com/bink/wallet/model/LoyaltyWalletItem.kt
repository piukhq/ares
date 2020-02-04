package com.bink.wallet.model

import com.bink.wallet.R
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.utils.EMPTY_STRING
import com.bink.wallet.utils.MembershipPlanUtils
import com.bink.wallet.utils.enums.CardType
import com.bink.wallet.utils.enums.LoginStatus
import com.bink.wallet.utils.enums.MembershipCardStatus
import com.bink.wallet.utils.formatBalance

class LoyaltyWalletItem(var membershipCard: MembershipCard, var membershipPlan: MembershipPlan) {

    // Region common use
    private fun shouldShowRetryStatus() =
        (membershipCard.status?.state == MembershipCardStatus.FAILED.status ||
                membershipCard.status?.state == MembershipCardStatus.UNAUTHORISED.status) &&
                membershipPlan.getCardType() != CardType.STORE


    private fun shouldShowPendingStatus() = (membershipPlan.getCardType() != CardType.STORE &&
            isPendingStatus(membershipCard)) ||
            (membershipCard.status?.state == MembershipCardStatus.AUTHORISED.status &&
                    MembershipPlanUtils.getAccountStatus(
                        membershipPlan,
                        membershipCard
                    ) == LoginStatus.STATUS_PENDING)

    private fun isPendingStatus(membershipCard: MembershipCard) =
        membershipCard.status?.state == MembershipCardStatus.PENDING.status


    // Region Linking Wrapper
    fun shouldShowLinkStatus() =
        membershipPlan.getCardType() == CardType.PLL

    fun shouldShowLinkImages() =
        shouldShowLinkStatus() &&
                membershipCard.status?.state == MembershipCardStatus.AUTHORISED.status

    fun retrieveLinkImage(): Int? {
        if (shouldShowLinkStatus()) {
            membershipCard.hasLinkedPaymentCards()?.let {
                return if (it) {
                    R.drawable.ic_linked
                } else {
                    R.drawable.ic_icons_unlinked
                }
            }
        }
        return null
    }

    fun retrieveLinkStatusText(): Int? {
        if (shouldShowLinkStatus()) {
            when {
                shouldShowRetryStatus() -> {
                    return R.string.card_status_retry
                }
                isPendingStatus(
                    membershipCard
                ) -> {
                    return R.string.card_status_pending
                }
                shouldShowLinkStatus() -> {
                    membershipCard.hasLinkedPaymentCards()?.let {
                        return if (it) {
                            R.string.loyalty_card_pll_linked
                        } else {
                            R.string.loyalty_card_pll_link_now
                        }
                    }
                }
            }
        }
        return null
    }

    //Region Auth Wrapper
    private fun shouldShowPointsSuffix() =
        shouldShowPoints() && membershipCard.balances?.first()?.suffix != null

    fun shouldShowPoints() =
        MembershipCardStatus.AUTHORISED.status == membershipCard.status?.state &&
                !membershipCard.balances.isNullOrEmpty()

    fun retrieveAuthStatusText(): Int? {
        return when {
            shouldShowPendingStatus() -> {
                R.string.card_status_pending
            }
            shouldShowRetryStatus() -> {
                R.string.card_status_retry
            }
            else -> {
                null
            }
        }
    }

    fun retrievePointsText(): String {
        val balance = membershipCard.balances?.first()
        return when (balance?.prefix != null) {
            true -> {
                balance.formatBalance()
            }
            else -> {
                balance?.value.toString()
            }
        }
    }

    fun retrieveAuthSuffix(): String {
        return if (shouldShowPointsSuffix()) membershipCard.balances?.first()
            ?.suffix.toString()
        else EMPTY_STRING
    }
}