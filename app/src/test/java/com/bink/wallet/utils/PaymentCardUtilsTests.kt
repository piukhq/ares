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
}