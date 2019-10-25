package com.bink.wallet.scenes.onboarding

import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.data.SharedPreferenceManager
import com.bink.wallet.databinding.OnboardingFragmentBinding
import com.bink.wallet.scenes.onboarding.OnboardingPagerAdapter.Companion.FIRST_PAGE_INDEX
import com.bink.wallet.scenes.onboarding.OnboardingPagerAdapter.Companion.ONBOARDING_PAGES_NUMBER
import com.bink.wallet.utils.*
import com.bink.wallet.utils.toolbar.FragmentToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

class OnboardingFragment : BaseFragment<OnboardingViewModel, OnboardingFragmentBinding>() {
    override val layoutRes: Int
        get() = R.layout.onboarding_fragment
    override val viewModel: OnboardingViewModel by viewModel()
    var timer = Timer()
    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .with(binding.toolbar)
            .build()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val adapter = fragmentManager?.let { OnboardingPagerAdapter(it) }
        adapter?.addFragment(OnboardingPageFragment.newInstance(
            PAGE_1,
            R.drawable.logo_page_1,
            getString(R.string.page_1_title),
            getString(R.string.page_1_description)
        ))
        adapter?.addFragment(OnboardingPageFragment.newInstance(
            PAGE_2,
            R.drawable.onb_2,
            getString(R.string.page_2_title),
            getString(R.string.page_2_description)))
        adapter?.addFragment(OnboardingPageFragment.newInstance(
            PAGE_3,
            R.drawable.onb_3,
            getString(R.string.page_3_title),
            getString(R.string.page_3_description)))
        binding.pager.adapter = adapter

        binding.logInEmail.setOnClickListener {
            findNavController().navigateIfAdded(this, R.id.onboarding_to_home)
        }

        binding.continueWithFacebook.setOnClickListener {
            requireContext().displayModalPopup(getString(R.string.missing_destination_dialog_title), getString(R.string.not_implemented_yet_text))
        }

        binding.signUpWithEmail.setOnClickListener {
            requireContext().displayModalPopup(getString(R.string.missing_destination_dialog_title), getString(R.string.not_implemented_yet_text))
        }

        binding.pager.addOnPageChangeListener(object: OnboardingPagerAdapter.CircularViewPagerHandler(binding.pager){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                scrollPagesAutomatically(binding.pager)
                if(position == 0) {
                    SharedPreferenceManager.isFirstOnboardingScreen = true
                    binding.back.visibility = View.GONE
                } else {
                    SharedPreferenceManager.isFirstOnboardingScreen = false
                    binding.back.visibility = View.VISIBLE
                }
                timer.cancel()
                timer = Timer()
                scrollPagesAutomatically(binding.pager)
            }
        })

        binding.back.setOnClickListener {
            binding.pager.arrowScroll(View.FOCUS_LEFT)
        }

        scrollPagesAutomatically(binding.pager)
    }

    private fun scrollPagesAutomatically(pager: ViewPager) {
        var currentPage = pager.currentItem
        val pagerHandler = Handler()
        val update = Runnable {
            if(currentPage == ONBOARDING_PAGES_NUMBER){
                pager.setCurrentItem(FIRST_PAGE_INDEX, true)
                currentPage = FIRST_PAGE_INDEX
            } else {
                pager.setCurrentItem(currentPage++, true)
            }
        }

        timer.schedule(object : TimerTask() {
            override fun run() {
                pagerHandler.post(update)
            }
        }, 0, ONBOARDING_SCROLL_DURATION_SECONDS)
    }
}