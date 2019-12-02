package com.bink.wallet.scenes.onboarding

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.OnboardingFragmentBinding
import com.bink.wallet.scenes.onboarding.OnboardingPagerAdapter.Companion.FIRST_PAGE_INDEX
import com.bink.wallet.scenes.onboarding.OnboardingPagerAdapter.Companion.ONBOARDING_PAGES_NUMBER
import com.bink.wallet.utils.*
import com.bink.wallet.utils.toolbar.FragmentToolbar
import com.facebook.*
import com.facebook.login.LoginResult
import org.json.JSONException
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
    private val FIELDS_KEY = "fields"
    private lateinit var callbackManager: CallbackManager
    private lateinit var facebookEmail: String
    private var accessToken: AccessToken? = null

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
                    R.drawable.onboarding_page_1,
                    getString(R.string.page_1_title),
                    getString(R.string.page_1_description)
                )
            )
            it.addFragment(
                OnboardingPageFragment.newInstance(
                    PAGE_2,
                    R.drawable.onboarding_page_2,
                    getString(R.string.page_2_title),
                    getString(R.string.page_2_description)
                )
            )
            it.addFragment(
                OnboardingPageFragment.newInstance(
                    PAGE_3,
                    R.drawable.onboarding_page_3,
                    getString(R.string.page_3_title),
                    getString(R.string.page_3_description)
                )
            )
            binding.pager.adapter = it
        }

        binding.logInEmail.setOnClickListener {
            findNavController().navigateIfAdded(this, R.id.onboarding_to_log_in)
        }
        binding.continueWithFacebook.fragment = this
        binding.continueWithFacebook.setReadPermissions(listOf(EMAIL_KEY))
        binding.continueWithFacebook.registerCallback(
            callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult?) {
                    result?.accessToken?.let {
                        retrieveFacebookLoginInformation(it)
                    }
                }

                override fun onCancel() {
                    requireContext().displayModalPopup(
                        null,
                        getString(R.string.facebook_cancelled)
                    )
                }

                override fun onError(error: FacebookException?) {
                    requireContext().displayModalPopup(
                        null,
                        getString(R.string.facebook_unavailable)
                    )
                }
            })

        binding.signUpWithEmail.setOnClickListener {
            findNavController().navigateIfAdded(this, R.id.onboarding_to_sign_up)
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
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    private fun scrollPagesAutomatically(pager: ViewPager) {
        var currentPage = pager.currentItem
        val pagerHandler = Handler()
        val update = Runnable {
            if (pager != null) {
                if (currentPage == ONBOARDING_PAGES_NUMBER) {
                    pager.setCurrentItem(FIRST_PAGE_INDEX, true)
                    currentPage = FIRST_PAGE_INDEX
                } else {
                    pager?.setCurrentItem(currentPage++, true)
                }
            }
        }

        timer.schedule(object : TimerTask() {
            override fun run() {
                pagerHandler.post(update)
            }
        }, 0, ONBOARDING_SCROLL_DURATION_SECONDS)
    }

    private fun retrieveFacebookLoginInformation(accessToken: AccessToken) {
        val request = GraphRequest.newMeRequest(
            accessToken
        ) { jsonObject, _ ->
            try {
                this.accessToken = accessToken
                facebookEmail = jsonObject.getString(EMAIL_KEY)
            } catch (e: JSONException) {
                if (!::facebookEmail.isInitialized) {
                    facebookEmail = getString(R.string.empty_string)
                }
                e.printStackTrace()
            }
            handleFacebookNavigation(facebookEmail)
        }
        val parameters = Bundle()
        parameters.putString(FIELDS_KEY, EMAIL_KEY)
        request.parameters = parameters
        request.executeAsync()
    }

    private fun handleFacebookNavigation(email: String?) {
        if (email.isNullOrEmpty()) {
            val directions =
                accessToken?.let { OnboardingFragmentDirections.onboardingToAddEmail(it) }
            directions?.let { findNavController().navigateIfAdded(this, it) }
        } else {
            val directions =
                accessToken?.let {
                    OnboardingFragmentDirections.onboardingToAcceptTc(
                        it,
                        facebookEmail
                    )
                }

            directions.let { _ ->
                directions?.let { findNavController().navigateIfAdded(this, it) }
            }
        }
    }
}