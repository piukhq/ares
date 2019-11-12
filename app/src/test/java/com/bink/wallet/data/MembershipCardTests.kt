package com.bink.wallet.data

import com.bink.wallet.model.response.membership_card.CardStatus
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.FeatureSet
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.model.response.payment_card.PaymentMembershipCard
import junit.framework.Assert.assertEquals
import org.junit.Test

class MembershipCardTests {
    @Test
    fun checkLinkStatusAuthButEmpty() {
        val card = MembershipCard("1",
            null,
            null,
            CardStatus(
                null,
                com.bink.wallet.utils.enums.CardStatus.AUTHORISED.status
            ),
            null,
            null,
            null,
            null)
        assertEquals("link_now", card.getLinkStatus())
    }
    @Test
    fun checkLinkStatus() {
        val card = MembershipCard("1",
            null,
            listOf(PaymentMembershipCard("1", true)),
            CardStatus(
                null,
                com.bink.wallet.utils.enums.CardStatus.AUTHORISED.status
            ),
            null,
            null,
            null,
            null)
        assertEquals("linked", card.getLinkStatus())
    }
    @Test
    fun checkLinkPending() {
        val card = MembershipCard("1",
            null,
            null,
            CardStatus(
                null,
                com.bink.wallet.utils.enums.CardStatus.UNAUTHORISED.status
            ),
            null,
            null,
            null,
            null)
        assertEquals("retry", card.getLinkStatus())
    }
    @Test
    fun checkLinkFailed() {
        val card = MembershipCard("1",
            null,
            null,
            CardStatus(
                null,
                com.bink.wallet.utils.enums.CardStatus.FAILED.status
            ),
            null,
            null,
            null,
            null)
        assertEquals("retry", card.getLinkStatus())
    }
    @Test
    fun checkLinkRetry() {
        val card = MembershipCard("1",
            null,
            null,
            CardStatus(
                null,
                com.bink.wallet.utils.enums.CardStatus.PENDING.status
            ),
            null,
            null,
            null,
            null)
        assertEquals("pending", card.getLinkStatus())
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
                null
            ),
            null,
            null,
            null
        )
        val card = MembershipCard(
            "1",
            null,
            null,
            CardStatus(
                null,
                com.bink.wallet.utils.enums.CardStatus.PENDING.status
            ),
            null,
            null,
            null,
            null)
        card.plan = plan
        assertEquals("", card.getLinkStatus())
    }
}