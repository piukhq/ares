package com.bink.wallet.utils

import junit.framework.Assert.assertFalse
import org.junit.Test

class PaymentCardUtilsTests {
    @Test
    fun testLuhnEmptyCard() {
        val cardNumber = ""
        assertFalse(cardNumber.luhnValidation())
    }

}