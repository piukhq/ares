package com.bink.wallet.utils

import junit.framework.Assert.assertEquals
import org.junit.Test

class BooleanUtilsTest {
    @Test
    fun testTrueToInt() {
        assertEquals(true.toInt(), 1)
    }

    @Test
    fun testFalseToInt() {
        assertEquals(false.toInt(), 0)
    }

    @Test
    fun testNullBoolToInt() {
        val bool: Boolean? = null
        assertEquals(bool.toInt(), 0)
    }
}