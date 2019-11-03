package com.bink.wallet.scenes.onboarding

import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
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
        with(adapter!!) {
            addFragment(
                OnboardingPageFragment.newInstance(
                    1,
                    PAGE_1,
                    R.drawable.logo_page_1,
                    getString(R.string.page_1_title) + " #1",
                    getString(R.string.page_1_description)
                )
            )
            addFragment(
                OnboardingPageFragment.newInstance(
                    2,
                    PAGE_2,
                    R.drawable.onb_2,
                    getString(R.string.page_2_title) + " #2",
                    getString(R.string.page_2_description)
                )
            )
            addFragment(
                OnboardingPageFragment.newInstance(
                    3,
                    PAGE_3,
                    R.drawable.onb_3,
                    getString(R.string.page_3_title) + " #3",
                    getString(R.string.page_3_description)
                )
            )
            binding.pager.adapter = this
            binding.pager.setCurrentItem(1, false)
        }

        binding.logInEmail.setOnClickListener {
            findNavController().navigateIfAdded(this, R.id.onboarding_to_home)
        }

        binding.continueWithFacebook.setOnClickListener {
            requireContext().displayModalPopup(
                getString(R.string.missing_destination_dialog_title),
                getString(R.string.not_implemented_yet_text)
            )
        }

        binding.signUpWithEmail.setOnClickListener {
            requireContext().displayModalPopup(
                getString(R.string.missing_destination_dialog_title),
                getString(R.string.not_implemented_yet_text)
            )
        }

        binding.pager.addOnPageChangeListener(object :
            OnboardingPagerAdapter.CircularViewPagerHandler(binding.pager) {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                scrollPagesAutomatically(binding.pager)

                binding.back.visibility =
                    if (position == 0) {
                        View.GONE
                    } else {
                        View.VISIBLE
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
/* NOTE: Disabling the timer for the moment as it threw an error:
   Cannot setMaxLifecycle for Fragment not attached to FragmentManager

   That needs to be looked at, plus the dots below,
   but getting the infinite scroller was the first step
 */
//        var currentPage = pager.currentItem
//        val pagerHandler = Handler()
//        val update = Runnable {
//            pager.setCurrentItem(currentPage++, true)
//        }
//        timer.schedule(object : TimerTask() {
//            override fun run() {
//                pagerHandler.post(update)
//            }
//        }, 0, ONBOARDING_SCROLL_DURATION_SECONDS)
    }
}