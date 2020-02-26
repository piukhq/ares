package com.bink.wallet.scenes.login

import android.graphics.Rect
import android.os.Bundle
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.util.Patterns
import android.view.View
import android.view.ViewTreeObserver
import androidx.navigation.fragment.findNavController
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.LoginFragmentBinding
import com.bink.wallet.model.request.SignUpRequest
import com.bink.wallet.utils.*
import com.bink.wallet.utils.FirebaseUtils.LOGIN_VIEW
import com.bink.wallet.utils.FirebaseUtils.getFirebaseIdentifier
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

    private val listener: ViewTreeObserver.OnGlobalLayoutListener =
        ViewTreeObserver.OnGlobalLayoutListener {
            val rec = Rect()
            binding.container.getWindowVisibleDisplayFrame(rec)
            val screenHeight = binding.container.rootView.height
            val keypadHeight = screenHeight - rec.bottom
            if (keypadHeight <= screenHeight * 0.15) {
                validateCredentials()
            }
        }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
    }

    override fun onResume() {
        super.onResume()
        logScreenView(LOGIN_VIEW)
        binding.container.viewTreeObserver.addOnGlobalLayoutListener(listener)
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

                findNavController().navigateIfAdded(
                    this@LoginFragment,
                    R.id.global_to_home
                )
            }

            logInErrorResponse.observeNonNull(this@LoginFragment) {
                isLoading.value = false
                if (!UtilFunctions.hasCertificatePinningFailed(it, requireContext())) {
                    requireContext().displayModalPopup(
                        EMPTY_STRING,
                        getString(R.string.incorrect_credentials)
                    )
                }
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
    }

    override fun onPause() {
        super.onPause()
        binding.container.viewTreeObserver.removeOnGlobalLayoutListener(listener)
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
