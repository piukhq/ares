package com.bink.wallet.utils

import com.bink.wallet.model.payment_card.PaymentCardType
import junit.framework.TestCase.*
import org.junit.Test

class PaymentCardUtilsTests {
    @Test
    fun testLuhnEmptyCard() {
        assertFalse("".luhnValidation())
    }

    @Test
    fun singleDigitStringsCannotBeValid() {
        assertFalse("1".luhnValidation())
    }

    @Test
    fun singleZeroIsInvalid() {
        assertFalse("0".luhnValidation())
    }

    @Test
    fun invalidWithNonDigit() {
        assertFalse("055a 444 285".luhnValidation())
    }

    @Test
    fun invalidLength() {
        assertFalse("0555 1444 2285".luhnValidation())
    }

    @Test
    fun invalidCard() {
        assertEquals("1242 4242 4242 4242".cardValidation(), PaymentCardType.NONE)
    }

    @Test
    fun validVisa() {
        assertEquals("4242 4242 4242 4242".cardValidation(), PaymentCardType.VISA)
    }

    @Test
    fun validMasterCard() {
        assertEquals("5336 1653 2182 8811".cardValidation(), PaymentCardType.MASTERCARD)
    }

    @Test
    fun validAmEx() {
        assertEquals("3400 00000 000009".cardValidation(), PaymentCardType.AMEX)
    }

    @Test
    fun checkPresentedVisa() {
        assertEquals("4242".presentedCardType(), PaymentCardType.VISA)
    }

    @Test
    fun checkPresentedMasterCard() {
        assertEquals("51".presentedCardType(), PaymentCardType.MASTERCARD)
    }

    @Test
    fun checkPresentedAmExSingle() {
        assertEquals("3".presentedCardType(), PaymentCardType.AMEX)
    }

    @Test
    fun checkPresentedAmEx() {
        assertEquals("34".presentedCardType(), PaymentCardType.AMEX)
    }

    @Test
    fun checkPresentedAmExFail() {
        assertEquals("31".presentedCardType(), PaymentCardType.NONE)
    }

    @Test
    fun checkLayoutShort() {
        assertEquals("42424242".cardFormatter(), "4242 4242")
    }
    @Test
    fun checkLayoutMedium() {
        assertEquals("424242424242".cardFormatter(), "4242 4242 4242")
    }
    @Test
    fun checkLayoutLong() {
        assertEquals("4242424242424242".cardFormatter(), "4242 4242 4242 4242")
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
        assertEquals("****", "4242".cardStarFormatter())
    }
    @Test
    fun checkStarLayoutShort() {
        assertEquals("**** ****", "42424242".cardStarFormatter())
    }
    @Test
    fun checkStarLayoutMedium() {
        assertEquals("**** **** ****", "424242424242".cardStarFormatter())
    }
    @Test
    fun checkStarLayoutLong() {
        assertEquals("**** **** **** 4242", "4242424242424242".cardStarFormatter())
    }
    @Test
    fun checkStarAmExLayoutLong() {
        assertEquals("**** **** **** 2424", "344242424242424".cardStarFormatter())
    }
}