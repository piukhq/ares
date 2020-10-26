package com.bink.wallet.scenes.onboarding

import android.os.Parcelable
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager


class OnboardingPagerAdapter(fragmentManager: FragmentManager) :
    FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    companion object {
        const val ONBOARDING_PAGES_NUMBER = 3
        const val FIRST_PAGE_INDEX = 0
    }

    private val onboardingFragments = mutableListOf<OnboardingPageFragment>()

    override fun getItem(position: Int): Fragment = onboardingFragments[position]

    override fun getCount(): Int = ONBOARDING_PAGES_NUMBER

    override fun saveState(): Parcelable? {
        return null
    }

    fun addFragment(fragment: OnboardingPageFragment) {
        onboardingFragments.add(fragment)
    }

    open class CircularViewPagerHandler(private val viewPager: ViewPager) :
        ViewPager.OnPageChangeListener {
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