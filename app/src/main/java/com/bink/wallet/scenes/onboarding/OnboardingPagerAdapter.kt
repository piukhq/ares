package com.bink.wallet.scenes.onboarding

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

class OnboardingPagerAdapter(fragmentManager: FragmentManager): FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private val ONBOARDING_PAGES_NUMBER = 3
    private val onboardingFragments = mutableListOf<OnboardingPageFragment>()

    override fun getItem(position: Int): Fragment  = onboardingFragments[position]

    override fun getCount(): Int =  ONBOARDING_PAGES_NUMBER

    fun addFragment(fragment: OnboardingPageFragment){
        onboardingFragments.add(fragment)
    }
}