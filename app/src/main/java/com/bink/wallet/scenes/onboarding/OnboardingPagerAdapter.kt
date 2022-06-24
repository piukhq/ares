package com.bink.wallet.scenes.onboarding

import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2

class OnboardingPagerAdapter(fragment: Fragment) :
    FragmentStateAdapter(fragment) {

    companion object {
        const val ONBOARDING_PAGES_NUMBER = 3
        const val FIRST_PAGE_INDEX = 0
    }

    private val onboardingFragments = mutableListOf<OnboardingPageFragment>()

    override fun getItemCount(): Int = ONBOARDING_PAGES_NUMBER

    override fun createFragment(position: Int): Fragment = onboardingFragments[position]

    fun addFragment(fragment: OnboardingPageFragment) {
        onboardingFragments.add(fragment)
    }

    open class CircularViewPagerHandler(private val viewPager: ViewPager2) :
        ViewPager2.OnPageChangeCallback() {
        private var currentPosition = 0
        private var scrollState = 0

        private val isScrollStateSettling: Boolean
            get() = scrollState == ViewPager.SCROLL_STATE_SETTLING

        override fun onPageSelected(position: Int) {
            currentPosition = position
        }

        override fun onPageScrollStateChanged(state: Int) {
            handleScrollState(state)
            scrollState = state
        }

        private fun handleScrollState(state: Int) {
            if (state == ViewPager.SCROLL_STATE_IDLE) {
                setNextItemIfNeeded()
            }
        }

        private fun setNextItemIfNeeded() {
            if (!isScrollStateSettling) {
                handleSetNextItem()
            }
        }

        private fun handleSetNextItem() {
            val lastPosition = ONBOARDING_PAGES_NUMBER - 1
            if (currentPosition == FIRST_PAGE_INDEX) {
                viewPager.setCurrentItem(lastPosition, false)
            } else if (currentPosition == lastPosition) {
                viewPager.setCurrentItem(FIRST_PAGE_INDEX, false)
            }
        }

        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {
        }
    }
}