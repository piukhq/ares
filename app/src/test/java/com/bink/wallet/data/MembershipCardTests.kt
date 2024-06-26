package com.bink.wallet.data

import com.bink.wallet.model.response.membership_card.CardStatus
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.FeatureSet
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.model.response.payment_card.PaymentMembershipCard
import com.bink.wallet.utils.enums.LoyaltyCardLinkStatus
import com.bink.wallet.utils.enums.MembershipCardStatus
import junit.framework.Assert.assertEquals
import org.junit.Test

class MembershipCardTests {
    @Test
    fun checkLinkStatusAuthButEmpty() {
        val card = makeMembershipCard(
            MembershipCardStatus.AUTHORISED
        )
        assertEquals(LoyaltyCardLinkStatus.LINK_NOW, card.getLinkStatus())
    }

    @Test
    fun checkLinkStatus() {
        val card = makeMembershipCard(
            MembershipCardStatus.AUTHORISED,
            listOf(PaymentMembershipCard("1", true))
        )
        assertEquals(LoyaltyCardLinkStatus.LINKED, card.getLinkStatus())
    }

    @Test
    fun checkLinkPending() {
        val card = makeMembershipCard(
            MembershipCardStatus.UNAUTHORISED
        )
        assertEquals(LoyaltyCardLinkStatus.RETRY, card.getLinkStatus())
    }

    @Test
    fun checkLinkFailed() {
        val card = makeMembershipCard(
            MembershipCardStatus.FAILED
        )
        assertEquals(LoyaltyCardLinkStatus.RETRY, card.getLinkStatus())
    }

    @Test
    fun checkLinkRetry() {
        val card = makeMembershipCard(
            MembershipCardStatus.PENDING
        )
        assertEquals(LoyaltyCardLinkStatus.PENDING, card.getLinkStatus())
    }

    @Test
    fun checkLinkEmpty() {
        val plan = MembershipPlan(
            "1",
            null,
            FeatureSet(
                null,
                null,
                null,
                null,
                1,
                null,
                null, false
            ),
            null,
            null,
            null,
            null, null, null
        )
        val card = makeMembershipCard(
            MembershipCardStatus.PENDING
        )
        card.plan = plan
        assertEquals(LoyaltyCardLinkStatus.NONE, card.getLinkStatus())
    }

    private fun makeMembershipCard(
        statusMembership: MembershipCardStatus,
        payment: List<PaymentMembershipCard>? = null
    ): MembershipCard {
        return MembershipCard(
            "1",
            null,
            payment,
            CardStatus(
                null,
                statusMembership.status
            ),
            null,
            null,
            null,
            null,
            null,
            null
        )
    }
}