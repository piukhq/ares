package com.bink.wallet.scenes.onboarding

import android.content.Intent
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
import com.bink.wallet.utils.*
import com.bink.wallet.utils.FirebaseUtils.ONBOARDING_VIEW
import com.bink.wallet.utils.FirebaseUtils.getFirebaseIdentifier
import com.bink.wallet.utils.UtilFunctions.isNetworkAvailable
import com.bink.wallet.utils.toolbar.FragmentToolbar
import com.facebook.*
import com.facebook.login.LoginBehavior
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import org.json.JSONException
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

    private val EMAIL_KEY = "email"
    private val FIELDS_KEY = "fields"
    private lateinit var callbackManager: CallbackManager
    private lateinit var facebookEmail: String
    private var accessToken: AccessToken? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FacebookSdk.sdkInitialize(context)
        LoginManager.getInstance().loginBehavior = LoginBehavior.WEB_VIEW_ONLY
        callbackManager = CallbackManager.Factory.create()
    }

    override fun onResume() {
        super.onResume()
        logScreenView(ONBOARDING_VIEW)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.clearWallets()

        val adapter = OnboardingPagerAdapter(childFragmentManager)
        adapter.let {
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

            logEvent(getFirebaseIdentifier(ONBOARDING_VIEW, binding.logInEmail.text.toString()))
        }

        binding.continueWithFacebook.setOnClickListener {
            binding.continueWithFacebook.apply {
                fragment = this@OnboardingFragment
                setReadPermissions(listOf(EMAIL_KEY))
                loginBehavior = LoginBehavior.WEB_VIEW_ONLY
                registerCallback(
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

                logEvent(
                    getFirebaseIdentifier(
                        ONBOARDING_VIEW,
                        binding.continueWithFacebook.text.toString()
                    )
                )
            }
        }
        binding.signUpWithEmail.setOnClickListener {
            findNavController().navigateIfAdded(this, R.id.onboarding_to_sign_up)

            logEvent(
                getFirebaseIdentifier(
                    ONBOARDING_VIEW,
                    binding.signUpWithEmail.text.toString()
                )
            )
        }
        with(binding.pager) {
            addOnPageChangeListener(object :
                OnboardingPagerAdapter.CircularViewPagerHandler(this) {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    scrollPagesAutomatically(this@with)
                    timer.cancel()
                    timer = Timer()
                    scrollPagesAutomatically(this@with)
                }
            })

            scrollPagesAutomatically(this)
        }
    }

    override fun onDestroy() {
        timer.apply {
            purge()
            cancel()
        }
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (isNetworkAvailable(requireActivity(), true)) {
            callbackManager.onActivityResult(requestCode, resultCode, data)
        }
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