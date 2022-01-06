package com.bink.wallet.scenes.onboarding

import android.os.Bundle
import android.os.Handler
import android.widget.Toolbar
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.OnboardingFragmentBinding
import com.bink.wallet.scenes.onboarding.OnboardingPagerAdapter.Companion.FIRST_PAGE_INDEX
import com.bink.wallet.scenes.onboarding.OnboardingPagerAdapter.Companion.ONBOARDING_PAGES_NUMBER
import com.bink.wallet.scenes.sign_up.SignUpFragmentDirections
import com.bink.wallet.utils.*
import com.bink.wallet.utils.FirebaseEvents.ONBOARDING_JOURNEY_LOGIN
import com.bink.wallet.utils.FirebaseEvents.ONBOARDING_JOURNEY_REGISTER
import com.bink.wallet.utils.FirebaseEvents.ONBOARDING_START
import com.bink.wallet.utils.FirebaseEvents.ONBOARDING_VIEW
import com.bink.wallet.utils.FirebaseEvents.getFirebaseIdentifier
import com.bink.wallet.utils.toolbar.FragmentToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

@Suppress("DEPRECATION")
class OnboardingFragment : BaseFragment<OnboardingViewModel, OnboardingFragmentBinding>() {
    override val layoutRes: Int
        get() = R.layout.onboarding_fragment
    override val viewModel: OnboardingViewModel by viewModel()
    var timer = Timer()
    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .with(Toolbar(requireContext()))
            .build()
    }

    override fun onResume() {
        super.onResume()
        logScreenView(ONBOARDING_VIEW)
        with(binding.pager) {
            scrollPagesAutomatically(this)
        }
    }

    override fun onPause() {
        resetTimer()
        super.onPause()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.clearWallets()

        val adapter = OnboardingPagerAdapter(childFragmentManager)
        adapter.let {
            it.addFragment(
                OnboardingPageFragment.newInstance(
                    PAGE_1,
                    R.drawable.ic_onboarding_page1,
                    getString(R.string.page_1_title),
                    getString(R.string.page_1_description)
                )
            )
            it.addFragment(
                OnboardingPageFragment.newInstance(
                    PAGE_2,
                    R.drawable.ic_onboarding_page2,
                    getString(R.string.page_2_title),
                    getString(R.string.page_2_description)
                )
            )
            it.addFragment(
                OnboardingPageFragment.newInstance(
                    PAGE_3,
                    R.drawable.ic_onboarding_page3,
                    getString(R.string.page_3_title),
                    getString(R.string.page_3_description)
                )
            )
            binding.pager.adapter = it
        }

        binding.signUpWithEmail.setOnClickListener {
            if (findNavController().currentDestination?.id == R.id.onboarding_fragment) {
                findNavController().navigateIfAdded(
                    this,
                    OnboardingFragmentDirections.onboardingToContinueWithEmail()
                )
            }
            logEvent(
                getFirebaseIdentifier(
                    ONBOARDING_VIEW,
                    binding.signUpWithEmail.text.toString()
                )
            )
            //ONBOARDING START FOR REGISTER
            logEvent(ONBOARDING_START, getOnboardingStartMap(ONBOARDING_JOURNEY_REGISTER))
        }
        with(binding.pager) {
            addOnPageChangeListener(object :
                OnboardingPagerAdapter.CircularViewPagerHandler(this) {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    scrollPagesAutomatically(this@with)
                    timer.cancel()
                    timer.purge()
                    timer = Timer()
                    scrollPagesAutomatically(this@with)
                }
            })
        }
    }

    private fun resetTimer() {
        timer.apply {
            cancel()
            purge()
        }
        timer = Timer()
    }

    private fun scrollPagesAutomatically(pager: ViewPager?) {
        var currentPage = pager?.currentItem as Int
        val pagerHandler = Handler()
        val update = Runnable {
            if (currentPage == ONBOARDING_PAGES_NUMBER) {
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