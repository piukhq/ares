package com.bink.wallet.scenes.onboarding

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.OnboardingFragmentBinding
import com.bink.wallet.scenes.onboarding.OnboardingPagerAdapter.Companion.FIRST_PAGE_INDEX
import com.bink.wallet.scenes.onboarding.OnboardingPagerAdapter.Companion.ONBOARDING_PAGES_NUMBER
import com.bink.wallet.utils.*
import com.bink.wallet.utils.toolbar.FragmentToolbar
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.FacebookSdk
import com.facebook.login.LoginResult
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
    private val EMAIL_KEY = "email"
    private val PUBLIC_PROFILE_KEY = "public_profile"
    private lateinit var callbackManager: CallbackManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FacebookSdk.sdkInitialize(context)

        callbackManager = CallbackManager.Factory.create()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val adapter = fragmentManager?.let { OnboardingPagerAdapter(it) }
        adapter?.let {
            it.addFragment(
                OnboardingPageFragment.newInstance(
                    PAGE_1,
                    R.drawable.logo_page_1,
                    getString(R.string.page_1_title),
                    getString(R.string.page_1_description)
                )
            )
            it.addFragment(
                OnboardingPageFragment.newInstance(
                    PAGE_2,
                    R.drawable.onb_2,
                    getString(R.string.page_2_title),
                    getString(R.string.page_2_description)
                )
            )
            it.addFragment(
                OnboardingPageFragment.newInstance(
                    PAGE_3,
                    R.drawable.onb_3,
                    getString(R.string.page_3_title),
                    getString(R.string.page_3_description)
                )
            )
            binding.pager.adapter = it
        }

        binding.logInEmail.setOnClickListener {
            findNavController().navigateIfAdded(this, R.id.onboarding_to_home)
        }
        binding.continueWithFacebook.setReadPermissions(listOf(EMAIL_KEY, PUBLIC_PROFILE_KEY))
        binding.continueWithFacebook.registerCallback(callbackManager, object: FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult?) {
                val accessToken = result?.accessToken
            }

            override fun onCancel() {
                Toast.makeText(requireContext(), getString(R.string.facebook_cancelled), Toast.LENGTH_LONG).show()
            }

            override fun onError(error: FacebookException?) {
                Toast.makeText(requireContext(), getString(R.string.facebook_unavailable), Toast.LENGTH_LONG).show()
            }
        })

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
                timer.cancel()
                timer = Timer()
                scrollPagesAutomatically(binding.pager)
            }
        })

        scrollPagesAutomatically(binding.pager)
    }

    override fun onDestroy() {
        timer.purge()
        timer.cancel()
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    private fun scrollPagesAutomatically(pager: ViewPager) {
        var currentPage = pager.currentItem
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