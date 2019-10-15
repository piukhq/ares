package com.bink.wallet.utils

import com.bink.wallet.utils.enums.PaymentCardType
import junit.framework.TestCase.*
import org.junit.Test

class PaymentCardUtilsTests {

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
