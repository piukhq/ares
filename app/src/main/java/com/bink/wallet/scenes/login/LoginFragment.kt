package com.bink.wallet.scenes.login

import android.os.Bundle
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.util.Patterns
import android.view.View
import androidx.navigation.fragment.findNavController
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.data.SharedPreferenceManager
import com.bink.wallet.databinding.LoginFragmentBinding
import com.bink.wallet.model.Consent
import com.bink.wallet.model.PostServiceConsent
import com.bink.wallet.model.request.SignUpRequest
import com.bink.wallet.utils.*
import com.bink.wallet.utils.FirebaseEvents.LOGIN_VIEW
import com.bink.wallet.utils.FirebaseEvents.getFirebaseIdentifier
import com.bink.wallet.utils.UtilFunctions.isNetworkAvailable
import com.bink.wallet.utils.toolbar.FragmentToolbar
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
        setupLayoutListener(binding.container, ::validateCredentials)
        registerLayoutListener(binding.container)
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
        viewModel.loginData.observeNonNull(this) {
            viewModel.authenticate()
        }

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
                viewModel.getMembershipPlans()
            }

            logInErrorResponse.observeErrorNonNull(
                requireContext(),
                this@LoginFragment,
                EMPTY_STRING,
                getString(R.string.incorrect_credentials),
                true
            ) {
                isLoading.value = false
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

            authErrorResponse.observeNonNull(this@LoginFragment) {
                // this is here for monitoring, but we don't need to report to the user at the moment
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
//                    viewModel.logIn(
//                        SignUpRequest(
//                            email = viewModel.email.value,
//                            password = viewModel.password.value
//                        )
//                    )
                    viewModel.postService(
                        PostServiceConsent(
                            consent = Consent(
                                email = viewModel.email.value!!,
                                timestamp = System.currentTimeMillis()
                            )
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
    }

    private fun initMembershipPlansObserver() {
        viewModel.membershipPlanDatabaseLiveData.observeNonNull(this@LoginFragment) {
            finishLogInProcess()
        }

        viewModel.membershipPlanErrorLiveData.observeNonNull(this@LoginFragment) {
            finishLogInProcess()
        }
    }

    private fun finishLogInProcess() {
        if (SharedPreferenceManager.isUserLoggedIn) {
            findNavController().navigate(LoginFragmentDirections.globalToHome())
        }
    }

    override fun onPause() {
        super.onPause()
        removeLayoutListener(binding.container)
    }

    private fun validateCredentials() {
        viewModel.email.value?.let {
            if (it.isNotEmpty()) {
                binding.emailField.error =
                    if (!Patterns.EMAIL_ADDRESS.matcher(it).matches()) {
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
}
