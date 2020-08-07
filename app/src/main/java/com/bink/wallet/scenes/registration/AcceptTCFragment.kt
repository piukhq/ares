package com.bink.wallet.scenes.registration

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.AcceptTcFragmentBinding
import com.bink.wallet.model.Consent
import com.bink.wallet.model.PostServiceRequest
import com.bink.wallet.model.auth.FacebookAuthRequest
import com.bink.wallet.model.request.MarketingOption
import com.bink.wallet.utils.FirebaseEvents.ONBOARDING_END
import com.bink.wallet.utils.FirebaseEvents.ONBOARDING_SERVICE_COMPLETE
import com.bink.wallet.utils.FirebaseEvents.ONBOARDING_SUCCESS_FALSE
import com.bink.wallet.utils.FirebaseEvents.ONBOARDING_SUCCESS_TRUE
import com.bink.wallet.utils.FirebaseEvents.ONBOARDING_USER_COMPLETE
import com.bink.wallet.utils.EMPTY_STRING
import com.bink.wallet.utils.FirebaseEvents.TERMS_AND_CONDITIONS_VIEW
import com.bink.wallet.utils.FirebaseEvents.getFirebaseIdentifier
import com.bink.wallet.utils.LocalStoreUtils
import com.bink.wallet.utils.UtilFunctions.isNetworkAvailable
import com.bink.wallet.utils.enums.MarketingOptions.MARKETING_OPTION_NO
import com.bink.wallet.utils.enums.MarketingOptions.MARKETING_OPTION_YES
import com.bink.wallet.utils.navigateIfAdded
import com.bink.wallet.utils.observeNetworkDrivenErrorNonNull
import com.bink.wallet.utils.observeNonNull
import com.bink.wallet.utils.setTermsAndPrivacyUrls
import com.bink.wallet.utils.toolbar.FragmentToolbar
import com.facebook.AccessToken
import com.facebook.login.LoginManager
import io.fabric.sdk.android.services.common.CommonUtils.hideKeyboard
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

class AcceptTCFragment : BaseFragment<AcceptTCViewModel, AcceptTcFragmentBinding>() {
    override val layoutRes: Int
        get() = R.layout.accept_tc_fragment

    override val viewModel: AcceptTCViewModel by viewModel()

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .with(binding.toolbar)
            .build()
    }

    private lateinit var termsAndConditionsHyperlink: String
    private lateinit var privacyPolicyHyperlink: String
    private lateinit var boldedTexts: Array<String>
    private var userEmail: String? = null
    private var accessToken: AccessToken? = null

    override fun onResume() {
        super.onResume()
        logScreenView(TERMS_AND_CONDITIONS_VIEW)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        termsAndConditionsHyperlink = getString(R.string.terms_conditions_text)
        privacyPolicyHyperlink = getString(R.string.privacy_policy_text)
        boldedTexts = resources.getStringArray(R.array.terms_bold_text_array)

        arguments?.let {
            with(AcceptTCFragmentArgs.fromBundle(it)) {
                userEmail = email
                this@AcceptTCFragment.accessToken = accessToken
            }
        }

        binding.termsAndConditionsText.setTermsAndPrivacyUrls(
            getString(R.string.terms_and_conditions_message),
            getString(R.string.terms_and_conditions_title),
            getString(R.string.privacy_policy_text),
            urlClickListener = { url ->
                findNavController().navigateIfAdded(
                    this@AcceptTCFragment,
                    AcceptTCFragmentDirections.globalToWeb(
                        url
                    ), R.id.accept_tcs_fragment
                )
            }
        )

        viewModel.facebookAuthError.observeNetworkDrivenErrorNonNull(
            requireContext(),
            this,
            getString(R.string.facebook_failed),
            EMPTY_STRING,
            true
        ) {
            handleAuthError()
            //FAILURE
            logEvent(ONBOARDING_END,getOnboardingEndMap(ONBOARDING_SUCCESS_FALSE))

        }

        viewModel.postServiceErrorResponse.observeNetworkDrivenErrorNonNull(
            requireContext(), this, getString(R.string.facebook_failed),
            EMPTY_STRING,
            true
        ) {
            handleAuthError()
            //FAILURE
            logEvent(ONBOARDING_SERVICE_COMPLETE,getOnboardingGenericMap())
            logEvent(ONBOARDING_END,getOnboardingEndMap(ONBOARDING_SUCCESS_FALSE))

        }

        viewModel.facebookAuthResult.observeNonNull(this) {
            LocalStoreUtils.setAppSharedPref(
                LocalStoreUtils.KEY_TOKEN,
                getString(R.string.token_api_v1, it.api_key)
            )
            userEmail?.let { userEmail ->
                viewModel.postService(
                    PostServiceRequest(
                        consent = Consent(
                            userEmail,
                            System.currentTimeMillis() / 1000
                        )
                    )
                )
            }

            if (binding.acceptMarketing.isChecked) {
                viewModel.handleMarketingPreferences(
                    MarketingOption(MARKETING_OPTION_YES.selected)
                )
            } else {
                viewModel.handleMarketingPreferences(
                    MarketingOption(
                        (MARKETING_OPTION_NO.selected)
                    )
                )
            }

            setFirebaseUserId(it.uid)
            //ONBOARDING USER COMPLETE
            logEvent(ONBOARDING_USER_COMPLETE,getOnboardingGenericMap())
        }

        viewModel.postServiceResponse.observeNonNull(this) {
            viewModel.getCurrentUser()
            //ONBOARDING SERVICE COMPLETE
            logEvent(ONBOARDING_SERVICE_COMPLETE,getOnboardingGenericMap())
            logEvent(ONBOARDING_END,getOnboardingEndMap(ONBOARDING_SUCCESS_TRUE))
        }


        binding.accept.setOnClickListener {
            startLoading()
            if (isNetworkAvailable(requireContext(), true)) {
                accessToken?.token?.let { token ->
                    accessToken?.userId?.let { userId ->
                        userEmail?.let { email ->
                            viewModel.authWithFacebook(
                                FacebookAuthRequest(
                                    token,
                                    email,
                                    userId
                                )
                            )
                        }
                    }
                }
            } else {
                stopLoading()
            }
            logEvent(
                getFirebaseIdentifier(
                    TERMS_AND_CONDITIONS_VIEW,
                    binding.accept.text.toString()
                )
            )
        }

        binding.back.setOnClickListener {
            LoginManager.getInstance().logOut()
            hideKeyboard(requireContext(), binding.root)
            findNavController().navigateIfAdded(this, R.id.accept_to_onboarding)
        }

        initMembershipPlansObserver()
        initUserDetailsObserver()
    }

    private fun initMembershipPlansObserver() {
        viewModel.membershipPlanDatabaseLiveData.observeNonNull(this@AcceptTCFragment) {
            finishLogInProcess()
        }

        viewModel.membershipPlanErrorLiveData.observeNonNull(this@AcceptTCFragment) {
            finishLogInProcess()
        }
    }

    private fun initUserDetailsObserver() {
        viewModel.getUserResponse.observeNonNull(this@AcceptTCFragment) {
            viewModel.getMembershipPlans()
        }
    }

    private fun finishLogInProcess() {
        stopLoading()
        findNavController().navigateIfAdded(
            this,
            AcceptTCFragmentDirections.acceptToLcd(true),
            R.id.accept_tcs_fragment
        )
    }

    private fun stopLoading() {
        viewModel.shouldAcceptBeEnabled.value = true
        binding.accept.isEnabled = true
    }

    private fun startLoading() {
        viewModel.shouldLoadingBeVisible.set(true)
        binding.accept.isEnabled = false
    }

    private fun handleAuthError() {
        binding.accept.isClickable = false
        val timer = Timer()
        context?.resources?.getInteger(R.integer.button_disabled_delay)?.toLong()
            ?.let { delay ->
                timer.schedule(object : TimerTask() {
                    override fun run() {
                        binding.accept.isClickable = true
                    }
                }, delay)
            }
    }
}