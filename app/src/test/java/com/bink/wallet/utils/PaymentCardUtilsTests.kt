package com.bink.wallet.utils

import junit.framework.TestCase.assertTrue
import junit.framework.TestCase.assertFalse
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
    fun validVisa() {
        assertTrue("4242 4242 4242 4242".cardValidation())
    }

    @Test
    fun validMasterCard() {
        assertTrue("5336 1653 2182 8811".cardValidation())
    }

    @Test
    fun validAmEx() {
        assertTrue("3400 00000 000009".cardValidation())
    }
}