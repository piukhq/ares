package com.bink.wallet.scenes.onboarding

import junit.framework.Assert.assertEquals
import org.junit.Test

class OnboardingPagerAdapterTests {
    @Test
    fun pageIdCheck_1a() {
        assertEquals(2, OnboardingPagerAdapter.getPageId(0, 0))
    }
    @Test
    fun pageIdCheck_1b() {
        assertEquals(0, OnboardingPagerAdapter.getPageId(1, 0))
    }
    @Test
    fun pageIdCheck_1c() {
        assertEquals(1, OnboardingPagerAdapter.getPageId(2, 0))
    }

    @Test
    fun pageIdCheck_2a() {
        assertEquals(0, OnboardingPagerAdapter.getPageId(0, 1))
    }
    @Test
    fun pageIdCheck_2b() {
        assertEquals(1, OnboardingPagerAdapter.getPageId(1, 1))
    }
    @Test
    fun pageIdCheck_2c() {
        assertEquals(2, OnboardingPagerAdapter.getPageId(2, 1))
    }

    @Test
    fun pageIdCheck_3a() {
        assertEquals(1, OnboardingPagerAdapter.getPageId(0, 2))
    }
    @Test
    fun pageIdCheck_3b() {
        assertEquals(2, OnboardingPagerAdapter.getPageId(1, 2))
    }
    @Test
    fun pageIdCheck_3c() {
        assertEquals(0, OnboardingPagerAdapter.getPageId(2, 2))
    }
}