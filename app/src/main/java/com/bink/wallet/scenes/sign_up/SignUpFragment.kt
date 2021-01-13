package com.bink.wallet.scenes.sign_up

import android.app.AlertDialog
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.navigation.fragment.findNavController
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.data.SharedPreferenceManager
import com.bink.wallet.databinding.SignUpFragmentBinding
import com.bink.wallet.model.Consent
import com.bink.wallet.model.PostServiceRequest
import com.bink.wallet.model.request.MarketingOption
import com.bink.wallet.model.request.SignUpRequest
import com.bink.wallet.utils.EMAIL_REGEX
import com.bink.wallet.utils.EMPTY_STRING
import com.bink.wallet.utils.FirebaseEvents.ONBOARDING_END
import com.bink.wallet.utils.FirebaseEvents.ONBOARDING_SERVICE_COMPLETE
import com.bink.wallet.utils.FirebaseEvents.ONBOARDING_SUCCESS_FALSE
import com.bink.wallet.utils.FirebaseEvents.ONBOARDING_SUCCESS_TRUE
import com.bink.wallet.utils.FirebaseEvents.ONBOARDING_USER_COMPLETE
import com.bink.wallet.utils.FirebaseEvents.REGISTER_VIEW
import com.bink.wallet.utils.FirebaseEvents.getFirebaseIdentifier
import com.bink.wallet.utils.LocalStoreUtils
import com.bink.wallet.utils.PASSWORD_REGEX
import com.bink.wallet.utils.UtilFunctions
import com.bink.wallet.utils.UtilFunctions.isNetworkAvailable
import com.bink.wallet.utils.displayModalPopup
import com.bink.wallet.utils.observeErrorNonNull
import com.bink.wallet.utils.observeNonNull
import com.bink.wallet.utils.setTermsAndPrivacyUrls
import com.bink.wallet.utils.toInt
import com.bink.wallet.utils.toolbar.FragmentToolbar
import com.bink.wallet.utils.validateEmail
import com.bink.wallet.utils.validatePassword
import org.koin.androidx.viewmodel.ext.android.viewModel
import retrofit2.HttpException

class SignUpFragment : BaseFragment<SignUpViewModel, SignUpFragmentBinding>() {

    override val layoutRes = R.layout.sign_up_fragment

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .with(binding.toolbar)
            .shouldDisplayBack(requireActivity())
            .build()
    }

    override val viewModel: SignUpViewModel by viewModel()

    private fun checkPasswordsMatch() =
        if (viewModel.password.value != viewModel.confirmPassword.value &&
            !viewModel.confirmPassword.value.isNullOrEmpty() &&
            !viewModel.password.value.isNullOrEmpty()
        ) {
            binding.confirmPasswordField.error = getString(R.string.password_not_match)
        } else {
            binding.confirmPasswordField.error = null
        }

    override fun onResume() {
        super.onResume()
        logScreenView(REGISTER_VIEW)
        setupKeyboardHiddenListener(binding.container, ::validateCredentials)
        registerKeyboardHiddenLayoutListener(binding.container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.signUpButton.isEnabled = false
        binding.lifecycleOwner = this
        viewModel.isLoading.value = false
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        with(binding) {
            viewModel = this@SignUpFragment.viewModel
            binding.checkboxTermsConditions.movementMethod = LinkMovementMethod.getInstance()
        }

        with(viewModel) {
            termsCondition.value = false
            marketingMessages.value = false

            email.observeNonNull(this@SignUpFragment) {
                requireContext().validateEmail(it, binding.emailField)
            }

            password.observeNonNull(this@SignUpFragment) {
                checkPasswordsMatch()
                requireContext().validatePassword(it, binding.passwordField)
            }

            binding.confirmPasswordField.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    checkPasswordsMatch()
                }
            }

            isLoading.observeNonNull(this@SignUpFragment) {
                with(binding) {
                    progressSpinner.visibility = when (it) {
                        true -> View.VISIBLE
                        else -> View.GONE
                    }
                }
            }

            signUpErrorResponse.observeErrorNonNull(requireContext(), this@SignUpFragment, true) {
                handleErrorResponse()
                showSignUpFailedDialog()
                //Failure
                logEvent(ONBOARDING_END,getOnboardingEndMap(ONBOARDING_SUCCESS_FALSE))

                viewModel.signUpErrorResponse.value = null
            }

            postServiceErrorResponse.observeErrorNonNull(
                requireContext(),
                this@SignUpFragment,
                true
            ) {
                handleErrorResponse()
                //ONBOARDING END WITH FAILURE
                logEvent(ONBOARDING_END,getOnboardingEndMap(ONBOARDING_SUCCESS_FALSE))
            }

            signUpResponse.observeNonNull(this@SignUpFragment) {
                LocalStoreUtils.setAppSharedPref(
                    LocalStoreUtils.KEY_TOKEN,
                    getString(R.string.token_api_v1, it.api_key)
                )

                LocalStoreUtils.setAppSharedPref(
                    LocalStoreUtils.KEY_EMAIL,
                    it.email ?: EMPTY_STRING
                )

                marketingPref(
                    MarketingOption(
                        marketingMessages.value.toInt()
                    )
                )
                viewModel.email.value?.let { email ->
                    viewModel.postService(
                        PostServiceRequest(
                            consent = Consent(
                                email,
                                System.currentTimeMillis() / 1000
                            )
                        )
                    )
                }

                it.uid?.let { uid ->
                    setAnalyticsUserId(uid)
                }
                //ONBOARDING USER COMPLETE
                logEvent(ONBOARDING_USER_COMPLETE,getOnboardingGenericMap())
            }

            postServiceResponse.observeNonNull(this@SignUpFragment) {
                getMembershipPlans()
                //ONBOARDING SERVICE COMPLETE
                logEvent(ONBOARDING_SERVICE_COMPLETE,getOnboardingGenericMap())
                //ONBOARDING END
                logEvent(ONBOARDING_END,getOnboardingEndMap(ONBOARDING_SUCCESS_TRUE))
            }
        }

        binding.progressSpinner.setOnTouchListener { _, _ -> true }

        binding.signUpButton.setOnClickListener {
            if (isNetworkAvailable(requireActivity(), true)) {
                requireContext().validateEmail(viewModel.email.value, binding.emailField)
                requireContext().validatePassword(viewModel.password.value, binding.passwordField)
                checkPasswordsMatch()

                with(viewModel) {
                    if (termsCondition.value == true) {
                        if (binding.confirmPasswordField.error == null &&
                            binding.passwordField.error == null &&
                            binding.emailField.error == null
                        ) {
                            isLoading.value = true
                            signUp(
                                SignUpRequest(
                                    email = email.value,
                                    password = password.value
                                )
                            )
                        } else {
                            requireContext().displayModalPopup(
                                null,
                                getString(R.string.all_fields_must_be_valid)
                            )
                        }
                    } else {
                        val dialogDescription = if (termsCondition.value != true) {
                            getString(
                                R.string.accept_tc_pp,
                                "${getString(R.string.terms_conditions_text)} & ${getString(R.string.privacy_policy_text)}"
                            )
                        } else {
                            if (termsCondition.value != true) {
                                getString(
                                    R.string.accept_tc_pp,
                                    getString(R.string.terms_conditions_text)
                                )
                            } else {
                                getString(
                                    R.string.accept_tc_pp,
                                    getString(R.string.privacy_policy_text)
                                )
                            }
                        }
                        requireContext().displayModalPopup(
                            EMPTY_STRING,
                            dialogDescription
                        )
                    }
                }
            }
            logEvent(getFirebaseIdentifier(REGISTER_VIEW, binding.signUpButton.text.toString()))
        }
        initMembershipPlansObserver()
        binding.termsAndConditionsText.setTermsAndPrivacyUrls(
            getString(R.string.terms_and_conditions_message),
            getString(R.string.terms_and_conditions_title),
            getString(R.string.privacy_policy_text),
            urlClickListener = { url ->
                findNavController().navigate(
                    SignUpFragmentDirections.globalToWeb(
                        url
                    )
                )
            }
        )


        binding.emailField.editText?.let {
            it.setOnFocusChangeListener { v, hasFocus ->
                requireContext().validateEmail(null,binding.emailField)
            }
        }


    }

    override fun onPause() {
        super.onPause()
        removeKeyboardHiddenLayoutListener(binding.container)
    }

    private fun validateCredentials() {
        viewModel.email.value?.let {
            if (it.isNotEmpty()) {
                binding.emailField.error =
                    if (!UtilFunctions.isValidField(EMAIL_REGEX, it)) {
                        getString(R.string.invalid_email_format)
                    } else {
                        null
                    }
            }
        }

        viewModel.password.value?.let {
            if (it.isNotEmpty()) {
                binding.passwordField.error =
                    if (!UtilFunctions.isValidField(PASSWORD_REGEX, it)) {
                        getString(R.string.password_description)
                    } else {
                        null
                    }
            }
        }

        viewModel.confirmPassword.value?.let {
            if (it.isNotEmpty()) {
                binding.confirmPasswordField.error =
                    if (it != viewModel.password.value) {
                        getString(R.string.password_not_match)
                    } else {
                        null
                    }
            }
        }
    }

    private fun handleErrorResponse() {
        viewModel.isLoading.value = false
    }

    private fun initMembershipPlansObserver() {
        viewModel.membershipPlanDatabaseLiveData.observeNonNull(this@SignUpFragment) {
            viewModel.isLoading.value = false
            finaliseAuthenticationFlow()
        }

        viewModel.membershipPlanErrorLiveData.observeNonNull(this@SignUpFragment) {
            finaliseAuthenticationFlow()
        }
    }

    private fun finaliseAuthenticationFlow() {
        if (SharedPreferenceManager.isUserLoggedIn) {
            findNavController().navigate(SignUpFragmentDirections.globalToHome(true))
        }
    }

    private fun showSignUpFailedDialog(){

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.error)
            .setMessage(
                getString(
                    R.string.error_sign_up_failed
                )
            )
            .setPositiveButton(
                getString(R.string.ok)
            ) { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }
}
