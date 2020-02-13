package com.bink.wallet.utils

import com.bink.wallet.utils.enums.PaymentCardType
import junit.framework.TestCase.*
import org.junit.Test

class PaymentCardUtilsTests {
    @Test
    fun testCcSanitizeShort() {
        assertEquals("12345678", "1234 5678".ccSanitize())
    }

    @Test
    fun testCcSanitizeMedium() {
        assertEquals("123456789012", "1234 5678 9012".ccSanitize())
    }

    @Test
    fun testSanitizeShort() {
        assertEquals("12345678", "1234 5678".numberSanitize())
    }

    @Test
    fun testSanitizeMedium() {
        assertEquals("123456789012", "1234 5678 9012".numberSanitize())
    }

    @Test
    fun testSanitizeLetters() {
        assertEquals("1234", "1234ABCD".numberSanitize())
    }

    @Test
    fun testLuhnEmptyCard() {
        assertFalse("".isValidLuhnFormat())
    }

    @Test
    fun singleDigitStringsCannotBeValid() {
        assertFalse("1".isValidLuhnFormat())
    }

    @Test
    fun singleZeroIsInvalid() {
        assertFalse("0".isValidLuhnFormat())
    }

    @Test
    fun invalidWithNonDigit() {
        assertFalse("055a 444 285".isValidLuhnFormat())
    }

    @Test
    fun invalidLength() {
        assertFalse("0555 1444 2285".isValidLuhnFormat())
    }

    @Test
    fun visaTooLong() {
        assertEquals(PaymentCardType.NONE, "4242 4242 4242 4242 4".cardValidation())
    }

    @Test
    fun amexTooLong() {
        assertEquals(PaymentCardType.NONE, "3424 424242 424242 4".cardValidation())
    }

    @Test
    fun invalidCard() {
        assertEquals(PaymentCardType.NONE, "1242 4242 4242 4242".cardValidation())
    }

    @Test
    fun validVisa() {
        assertEquals(PaymentCardType.VISA, "4242 4242 4242 4242".cardValidation())
    }

    @Test
    fun validMasterCard() {
        assertEquals(PaymentCardType.MASTERCARD, "5336 1653 2182 8811".cardValidation())
    }

    @Test
    fun validAmEx() {
        assertEquals(PaymentCardType.AMEX, "3400 00000 000009".cardValidation())
    }

    @Test
    fun checkPresentedVisa() {
        assertEquals(PaymentCardType.VISA, "4242".presentedCardType())
    }

    @Test
    fun checkPresentedMasterCard() {
        assertEquals(PaymentCardType.MASTERCARD, "51".presentedCardType())
    }

    @Test
    fun checkPresentedMasterCardBIN() {
        assertEquals(PaymentCardType.MASTERCARD_BIN, "27".presentedCardType())
    }

    @Test
    fun checkPresentedAmExSingle() {
        assertEquals(PaymentCardType.AMEX, "3".presentedCardType())
    }

    @Test
    fun checkPresentedAmEx() {
        assertEquals(PaymentCardType.AMEX, "34".presentedCardType())
    }

    @Test
    fun checkPresentedAmExFail() {
        assertEquals(PaymentCardType.NONE, "31".presentedCardType())
    }

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
        assertEquals("****", "4242".cardStarFormatter())
    }

    @Test
    fun checkStarLayoutShort() {
        assertEquals("**** ****", "42424242".cardStarFormatter())
    }

    @Test
    fun checkStarLayoutMedium() {
        assertEquals("**** **** ****", "4242 4242 4242".cardStarFormatter())
    }

    @Test
    fun checkStarLayoutAlmostFull() {
        assertEquals("**** **** **** 42", "4242 4242 4242 42".cardStarFormatter())
    }

    @Test
    fun checkStarLayoutLong() {
        assertEquals("**** **** **** 4242", "4242 4242 4242 4242".cardStarFormatter())
    }

    @Test
    fun checkStarAmExLayoutAlmostFull() {
        assertEquals("**** **** **** 242", "3442 424242 4242".cardStarFormatter())
    }

    @Test
    fun checkStarAmExLayoutLong() {
        assertEquals("**** **** **** 2424", "3442 424242 42424".cardStarFormatter())
    }

    @Test
    fun emptyDateTest() {
        assertFalse("".dateValidation())
    }

    @Test
    fun garbageDateTest() {
        assertFalse("abcde".dateValidation())
    }

    @Test
    fun testSimpleDateFormat() {
        assertEquals("01/01", "101".formatDate())
    }

    @Test
    fun testDateFormat() {
        assertEquals("11/21", "11/21".formatDate())
    }

    @Test
    fun validateMonthTooSmall() {
        assertFalse("00/20".dateValidation())
    }

    @Test
    fun validateMonthTooLarge() {
        assertFalse("13/20".dateValidation())
    }

    @Test
    fun validateYearTooSmall() {
        assertFalse("10/10".dateValidation())
    }

    @Test
    fun validateYearTooLarge() {
        assertFalse("10/40".dateValidation())
    }

    @Test
    fun validateDateNoSlash() {
        assertTrue("1021".dateValidation())
    }

    @Test
    fun validateDateWithSlash() {
        assertTrue("10/21".dateValidation())
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

    @Test
    fun testMasterCardTwoLow() {
        assertEquals("2221".presentedCardType(), PaymentCardType.MASTERCARD_BIN)
    }

    @Test
    fun testMasterCardTwoLowFail() {
        assertEquals("2220".presentedCardType(), PaymentCardType.NONE)
    }

    @Test
    fun testMasterCardTwoHigh() {
        assertEquals("272099".presentedCardType(), PaymentCardType.MASTERCARD_BIN)
    }

    @Test
    fun testMasterCardTwoHighFail() {
        assertEquals("2721".presentedCardType(), PaymentCardType.NONE)
    }

    @Test
    fun testMasterCardTwoMid() {
        assertEquals("23".presentedCardType(), PaymentCardType.MASTERCARD_BIN)
    }

    @Test
    fun testExpiryMissingYear() {
        assertFalse("12/".dateValidation())
    }

    @Test
    fun testExpiryMissingMonth() {
        assertFalse("/19".dateValidation())
    }

    @Test
    fun testExpiryMissingMonthAndYear() {
        assertFalse("/".dateValidation())
    }

    @Test
    fun testExpiryValid() {
        assertTrue("12/20".dateValidation())
    }
}
