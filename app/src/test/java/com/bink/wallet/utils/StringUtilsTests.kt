package com.bink.wallet.utils

import junit.framework.Assert.assertEquals
import org.junit.Test

class StringUtilsTests {
    @Test
    fun testRandomLength() {
        assertEquals(100, StringUtils.randomString(100).length)
    }

    @Test
    fun testRandomRegex() {
        assert(StringUtils.randomString(100).matches(Regex("[A-Za-z0-9]*")))

    }
}