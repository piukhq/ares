package com.bink.wallet.scenes.onboarding

import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.MutableLiveData
import androidx.viewpager.widget.ViewPager

class OnboardingPagerAdapter(fragmentManager: FragmentManager) :
    FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    companion object {
        const val ONBOARDING_PAGES_NUMBER = 3
        const val FIRST_PAGE_INDEX = 0
        const val MIDDLE_PAGE_INDEX = 1

        var currentPage = FIRST_PAGE_INDEX
        val onboardingFragments = mutableListOf<OnboardingPageFragment>()
        val storedFragments = mutableListOf<OnboardingPageFragment>()

        fun getPageId(page: Int, current: Int = currentPage): Int {
            val newPage = page + current - 1
            return when {
                newPage < 0 -> ONBOARDING_PAGES_NUMBER + newPage
                newPage >= ONBOARDING_PAGES_NUMBER -> newPage - ONBOARDING_PAGES_NUMBER
                else -> newPage
            }
        }
    }

    override fun getItem(position: Int): OnboardingPageFragment {
        val newId = getPageId(position, 0)
        return onboardingFragments[newId]
    }

    override fun getItemPosition(obj: Any): Int {
        for (i in 0 until ONBOARDING_PAGES_NUMBER) {
            if ((obj as OnboardingPageFragment).pageId == onboardingFragments[i].pageId)
                return i
        }
        return super.getItemPosition(obj)
    }

    override fun getCount(): Int = ONBOARDING_PAGES_NUMBER

    fun addFragment(fragment: OnboardingPageFragment) {
        onboardingFragments.add(fragment)
        storedFragments.add(fragment)
    }

    open class CircularViewPagerHandler(private val viewPager: ViewPager, val pageId: MutableLiveData<Int>) :
        ViewPager.OnPageChangeListener {
        private var currentPosition = 1
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
//            if (!isScrollStateSettling) {
                handleSetNextItem()
//            }
        }

        private fun handleSetNextItem() {
            val updatedPage = getPageId(currentPosition)
            if (updatedPage != currentPage) {
                pageId.value = updatedPage
                currentPage = updatedPage
                setupPages()
                viewPager.adapter!!.notifyDataSetChanged()
                viewPager.setCurrentItem(MIDDLE_PAGE_INDEX, false)
            }
        }

        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {
        }

        fun setupPages() {
            val updated = ArrayList<OnboardingPageFragment>()
            for (i in 0 until ONBOARDING_PAGES_NUMBER) {
                var whichPage = currentPage + i - 1
                whichPage = when {
                    whichPage < 0 -> ONBOARDING_PAGES_NUMBER + whichPage
                    whichPage >= ONBOARDING_PAGES_NUMBER -> whichPage - ONBOARDING_PAGES_NUMBER
                    else -> whichPage
                }
                updated.add(storedFragments[whichPage])
            }
            onboardingFragments.clear()
            onboardingFragments.addAll(updated)
            viewPager.adapter!!.notifyDataSetChanged()
        }
    }
}