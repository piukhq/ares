package com.bink.wallet.scenes.login

import android.os.Bundle
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.navigation.fragment.findNavController
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.data.SharedPreferenceManager
import com.bink.wallet.databinding.LoginFragmentBinding
import com.bink.wallet.model.Consent
import com.bink.wallet.model.PostServiceRequest
import com.bink.wallet.model.request.SignUpRequest
import com.bink.wallet.utils.EMAIL_REGEX
import com.bink.wallet.utils.EMPTY_STRING
import com.bink.wallet.utils.FirebaseEvents.LOGIN_VIEW
import com.bink.wallet.utils.FirebaseEvents.ONBOARDING_END
import com.bink.wallet.utils.FirebaseEvents.ONBOARDING_SERVICE_COMPLETE
import com.bink.wallet.utils.FirebaseEvents.ONBOARDING_SUCCESS_FALSE
import com.bink.wallet.utils.FirebaseEvents.ONBOARDING_SUCCESS_TRUE
import com.bink.wallet.utils.FirebaseEvents.ONBOARDING_USER_COMPLETE
import com.bink.wallet.utils.FirebaseEvents.getFirebaseIdentifier
import com.bink.wallet.utils.LocalStoreUtils
import com.bink.wallet.utils.PASSWORD_REGEX
import com.bink.wallet.utils.UtilFunctions
import com.bink.wallet.utils.UtilFunctions.isNetworkAvailable
import com.bink.wallet.utils.displayModalPopup
import com.bink.wallet.utils.navigateIfAdded
import com.bink.wallet.utils.observeNetworkDrivenErrorNonNull
import com.bink.wallet.utils.observeNonNull
import com.bink.wallet.utils.toolbar.FragmentToolbar
import com.bink.wallet.utils.validateEmail
import com.bink.wallet.utils.validatePassword
import com.google.android.material.textfield.TextInputLayout
import org.koin.androidx.viewmodel.ext.android.viewModel


class LoginFragment : BaseFragment<LoginViewModel, LoginFragmentBinding>() {
    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .with(binding.toolbar)
            .shouldDisplayBack(requireActivity())
            .build()
    }

    override val layoutRes: Int
        get() = R.layout.login_fragment

    override val viewModel: LoginViewModel by viewModel()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
    }

    override fun onResume() {
        super.onResume()
        logScreenView(LOGIN_VIEW)
        setupKeyboardHiddenListener(binding.container, ::validateCredentials)
        registerKeyboardHiddenLayoutListener(binding.container)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val content = SpannableString(getString(R.string.forgot_password_text))
        content.setSpan(UnderlineSpan(), 0, content.length, 0)

        with(binding.forgotPassword) {
            text = content
            setOnClickListener {
                findNavController().navigateIfAdded(
                    this@LoginFragment,
                    R.id.login_to_forgot_password
                )
                logEvent(getFirebaseIdentifier(LOGIN_VIEW, binding.forgotPassword.text.toString()))
            }
        }

        binding.logInButton.isEnabled = false

        viewModel.retrieveStoredLoginData()
        binding.viewModel = viewModel

        with(viewModel) {
            email.observeNonNull(this@LoginFragment) {
                requireContext().validateEmail(it, binding.emailField)
            }

            password.observeNonNull(this@LoginFragment) {
                requireContext().validatePassword(it, binding.passwordField)
            }

            logInResponse.observeNonNull(this@LoginFragment) {
                LocalStoreUtils.setAppSharedPref(
                    LocalStoreUtils.KEY_TOKEN,
                    getString(R.string.token_api_v1, it.api_key)
                )

                LocalStoreUtils.setAppSharedPref(
                    LocalStoreUtils.KEY_EMAIL,
                    it.email ?: EMPTY_STRING
                )

                email.value?.let { email ->
                    postService(
                        PostServiceRequest(
                            consent = Consent(
                                email = email,
                                timestamp = System.currentTimeMillis() / 1000
                            )
                        )
                    )
                }

                postServiceResponse.observeNonNull(this@LoginFragment) {
                    getCurrentUser()
                    //onboarding-service-complete for LOGIN
                    logEvent(ONBOARDING_SERVICE_COMPLETE, getOnboardingGenericMap())
                    //onboarding-end with true
                    logEvent(ONBOARDING_END, getOnboardingEndMap(ONBOARDING_SUCCESS_TRUE))
                }

                it.uid?.let { uid ->
                    setAnalyticsUserId(uid)
                }

                //onboarding-user-complete for LOGIN
                logEvent(ONBOARDING_USER_COMPLETE, getOnboardingGenericMap())
            }

            logInErrorResponse.observeNetworkDrivenErrorNonNull(
                requireContext(),
                this@LoginFragment,
                EMPTY_STRING,
                getString(R.string.incorrect_credentials),
                true
            ) {
                handleErrorResponse()
                //SHOULD WE COUNT THIS AS END OF JOURNEY?
                logEvent(ONBOARDING_END, getOnboardingEndMap(ONBOARDING_SUCCESS_FALSE))
            }

            postServiceErrorResponse.observeNetworkDrivenErrorNonNull(
                requireContext(),
                this@LoginFragment,
                EMPTY_STRING,
                getString(R.string.incorrect_credentials),
                true
            ) {
                handleErrorResponse()
                //onboarding-service-complete for LOGIN
                logEvent(ONBOARDING_USER_COMPLETE, getOnboardingGenericMap())
                //onboarding-end with false
                logEvent(ONBOARDING_END, getOnboardingEndMap(ONBOARDING_SUCCESS_FALSE))
            }

            isLoading.observeNonNull(this@LoginFragment) {
                with(binding) {
                    progressSpinner.visibility = when (it) {
                        true -> View.VISIBLE
                        else -> View.GONE
                    }

                    logInButton.isEnabled = !it
                }
            }
        }

        binding.logInButton.setOnClickListener {
            if (isNetworkAvailable(requireActivity(), true)) {
                it.isEnabled = false
                requireContext().apply {
                    validateEmail(viewModel.email.value, binding.emailField)
                    validatePassword(viewModel.password.value, binding.passwordField)
                }
                if (binding.passwordField.error == null &&
                    binding.emailField.error == null
                ) {
                    binding.progressSpinner.visibility = View.VISIBLE
                    viewModel.logIn(
                        SignUpRequest(
                            email = viewModel.email.value,
                            password = viewModel.password.value
                        )
                    )
                } else {
                    requireContext().displayModalPopup(
                        null,
                        getString(R.string.all_fields_must_be_valid)
                    )
                }
            }

            logEvent(getFirebaseIdentifier(LOGIN_VIEW, binding.logInButton.text.toString()))
        }

        initMembershipPlansObserver()
        initUserDetailsObserver()

        with(binding.emailField) {
            this.editText?.let {
                it.setOnFocusChangeListener { _, _ ->
                    if (it.text.isNotEmpty()) binding.emailField.isEndIconVisible = true
                    requireContext().validateEmail(null,binding.emailField)
                }

                it.setOnEditorActionListener { _, actionId, _ ->
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        requireContext().validateEmail(null,binding.emailField)
                    }
                    false
                }
            }
        }

        with(binding.passwordField) {
            this.editText?.let {
                it.setOnFocusChangeListener { _, _ ->
                    requireContext().validatePassword(null,binding.passwordField)
                }

                it.setOnEditorActionListener { _, actionId, _ ->
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        validatePasswordAfterKeyboardExit(this)
                    }
                    false
                }
            }
        }
    }

    private fun initMembershipPlansObserver() {
        viewModel.membershipPlanDatabaseLiveData.observeNonNull(this@LoginFragment) {
            finishLogInProcess()
        }

        viewModel.membershipPlanErrorLiveData.observeNonNull(this@LoginFragment) {
            finishLogInProcess()
        }
    }

    private fun initUserDetailsObserver() {
        viewModel.getUserResponse.observeNonNull(this@LoginFragment) {
            viewModel.getMembershipPlans()
            setAnalyticsUserId(it.uid)
        }
    }

    private fun finishLogInProcess() {
        if (SharedPreferenceManager.isUserLoggedIn) {
            findNavController().navigate(LoginFragmentDirections.globalToHome(true))
        }
    }

    private fun handleErrorResponse() {
        viewModel.isLoading.value = false
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
    }

    private fun validatePasswordAfterKeyboardExit(textInputLayout: TextInputLayout){
        textInputLayout.editText?.let {
            if ((!it.text.isNullOrEmpty() &&
                        !UtilFunctions.isValidField(
                            PASSWORD_REGEX,
                            it.text.toString()
                        )) || (it.text.isNullOrEmpty())
            ) {
                textInputLayout.error =
                    getString(R.string.password_description)
            } else {
                textInputLayout.error = null
                textInputLayout.isErrorEnabled = false
            }
        }

    }
}
