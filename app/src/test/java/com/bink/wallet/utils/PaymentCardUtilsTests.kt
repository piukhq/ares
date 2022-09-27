package com.bink.wallet.utils

import com.bink.wallet.utils.enums.PaymentCardType
import junit.framework.TestCase.*
import org.junit.Test

class PaymentCardUtilsTests {
    @Test
    fun checkLayoutShort() {
        assertEquals("4242 4242", "42424242".cardFormatter())
    }

    @Test
    fun checkLayoutMedium() {
        assertEquals("4242 4242 4242", "424242424242".cardFormatter())
    }

    @Test
    fun checkLayoutLong() {
        assertEquals("4242 4242 4242 4242", "4242424242424242".cardFormatter())
    }

    @Test
    fun checkAmExLayoutLong() {
        assertEquals("3424 242424 24242", "342424242424242".cardFormatter())
    }

    @Test
    fun checkStarLayoutEmpty() {
        assertEquals("", "".cardStarFormatter())
    }

    @Test
    fun checkStarLayoutInvalid() {
        assertEquals("", "9427".cardStarFormatter())
    }

    @Test
    fun checkStarLayoutVeryShort() {
        assertEquals("••••", "4242".cardStarFormatter())
    }

    @Test
    fun checkStarLayoutShort() {
        assertEquals("•••• ••••", "42424242".cardStarFormatter())
    }

    @Test
    fun checkStarLayoutMedium() {
        assertEquals("•••• •••• ••••", "4242 4242 4242".cardStarFormatter())
    }

    @Test
    fun checkStarLayoutAlmostFull() {
        assertEquals("•••• •••• •••• 42", "4242 4242 4242 42".cardStarFormatter())
    }

    @Test
    fun checkStarLayoutLong() {
        assertEquals("•••• •••• •••• 4242", "4242 4242 4242 4242".cardStarFormatter())
    }

    @Test
    fun checkStarAmExLayoutAlmostFull() {
        assertEquals("•••• •••• •••• 242", "3442 424242 4242".cardStarFormatter())
    }

    @Test
    fun checkStarAmExLayoutLong() {
        assertEquals("•••• •••• •••• 2424", "3442 424242 42424".cardStarFormatter())
    }

    @Test
    fun testCardIsVisa() {
        assertEquals("Visa".getCardType(), PaymentCardType.VISA)
    }

    @Test
    fun testCardIsMastercard() {
        assertEquals("Mastercard".getCardType(), PaymentCardType.MASTERCARD)
    }

    @Test
    fun testCardIsAmex() {
        assertEquals("American Express".getCardType(), PaymentCardType.AMEX)
    }

    @Test
    fun testCardIsUnknown() {
        assertEquals("Test".getCardType(), PaymentCardType.NONE)
    }

}
