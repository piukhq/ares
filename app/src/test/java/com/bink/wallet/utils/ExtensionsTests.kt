package com.bink.wallet.utils

import junit.framework.Assert.assertEquals
import org.junit.Test

class ExtensionsTests {
    @Test
    fun testBoolToIntPositive() {
        assertEquals(true.toInt(), 1)
    }
    @Test
    fun testBoolToIntNegative() {
        assertEquals(false.toInt(), 0)
    }
    @Test
    fun testBoolToIntNull() {
        val bool: Boolean? = null
        assertEquals(bool.toInt(), 0)
    }
}