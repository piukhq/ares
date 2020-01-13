package com.bink.wallet.scenes.login

import android.os.Bundle
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.View
import androidx.navigation.fragment.findNavController
import com.bink.wallet.BaseFragment
import com.bink.wallet.MainActivity
import com.bink.wallet.R
import com.bink.wallet.databinding.LoginFragmentBinding
import com.bink.wallet.model.request.SignUpRequest
import com.bink.wallet.utils.*
import com.bink.wallet.utils.toolbar.FragmentToolbar
import kotlinx.coroutines.runBlocking
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

    private fun setLoginButtonEnableStatus() {
        with(binding) {
            viewModel?.let {
                logInButton.isEnabled =
                    (passwordField.error == null &&
                            emailField.error == null &&
                            (it.email.value ?: EMPTY_STRING).isNotBlank() &&
                            (it.password.value ?: EMPTY_STRING).isNotBlank()
                            )
            }
        }
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
            }
        }

        binding.logInButton.isEnabled = false

        viewModel.retrieveStoredLoginData()
        binding.viewModel = viewModel
        viewModel.loginData.observeNonNull(this) {
            if (verifyAvailableNetwork(requireActivity())) {
                viewModel.authenticate()
            } else {
                showNoInternetConnectionDialog()
            }
        }

        with(viewModel) {
            email.observeNonNull(this@LoginFragment) {
                requireContext().validateEmail(it, binding.emailField)
                setLoginButtonEnableStatus()
            }

            password.observeNonNull(this@LoginFragment) {
                requireContext().validatePassword(it, binding.passwordField)
                setLoginButtonEnableStatus()
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

                runBlocking {
                    (activity as MainActivity).fetchData()
                }

                findNavController().navigateIfAdded(
                    this@LoginFragment,
                    R.id.global_to_home
                )
            }

            logInErrorResponse.observeNonNull(this@LoginFragment) {
                isLoading.value = false
                requireContext().displayModalPopup(
                    EMPTY_STRING,
                    getString(R.string.incorrect_credentials)
                )
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
            requireContext().validateEmail(viewModel.email.value, binding.emailField)
            requireContext().validatePassword(viewModel.password.value, binding.passwordField)

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
    }
}
