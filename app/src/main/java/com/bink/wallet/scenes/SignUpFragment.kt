package com.bink.wallet.scenes

import android.os.Bundle
import android.util.Patterns
import androidx.core.text.HtmlCompat
import androidx.navigation.fragment.findNavController
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.SignUpFragmentBinding
import com.bink.wallet.model.request.MarketingOption
import com.bink.wallet.model.request.SignUpRequest
import com.bink.wallet.utils.*
import com.bink.wallet.utils.toolbar.FragmentToolbar
import kotlinx.coroutines.runBlocking
import org.koin.androidx.viewmodel.ext.android.viewModel

class SignUpFragment : BaseFragment<SignUpViewModel, SignUpFragmentBinding>() {

    override val layoutRes = R.layout.sign_up_fragment

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .with(binding.toolbar)
            .shouldDisplayBack(requireActivity())
            .build()
    }

    override val viewModel: SignUpViewModel by viewModel()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        with(binding) {
            signUpFooterMessage.text = HtmlCompat.fromHtml(
                getString(R.string.sign_up_footer_text),
                HtmlCompat.FROM_HTML_MODE_LEGACY
            )
            viewModel = this@SignUpFragment.viewModel
        }

        with(viewModel) {
            email.observeNonNull(this@SignUpFragment) {
                if (!Patterns.EMAIL_ADDRESS.matcher(it).matches()) {
                    binding.emailField.error = getString(R.string.incorrect_email_text)
                } else {
                    binding.emailField.error = null
                }
            }

            password.observeNonNull(this@SignUpFragment) {
                if (!UtilFunctions.isValidField(PASSWORD_REGEX, it)) {
                    binding.passwordField.error =
                        getString(R.string.password_description)
                } else {
                    binding.passwordField.error = null
                }
            }

            confirmPassword.observeNonNull(this@SignUpFragment) {
                if (!UtilFunctions.isValidField(PASSWORD_REGEX, it)) {
                    binding.passwordField.error =
                        getString(R.string.password_description)
                } else {
                    if (password.value != it) {
                        binding.confirmPasswordField.error = getString(R.string.password_not_match)
                    } else {
                        binding.confirmPasswordField.error = null
                    }
                }
            }
        }

        viewModel.signUpResponse.observeNonNull(this) {
            runBlocking {
                LocalStoreUtils.setAppSharedPref(
                    LocalStoreUtils.KEY_JWT_V1,
                    getString(R.string.token_api_v1, it.api_key),
                    requireContext()
                )

                viewModel.marketingPref(
                    MarketingOption(
                        when (viewModel.marketingMessages.value) {
                            true -> 1
                            else -> 0
                        }
                    )
                )

                findNavController().navigateIfAdded(this@SignUpFragment, R.id.global_to_home)
            }
        }

        binding.signUpButton.setOnClickListener {
            with(viewModel) {
                if (termsCondition.value == true &&
                    privacyPolicy.value == true
                ) {
                    if (binding.confirmPasswordField.error == null &&
                        binding.passwordField.error == null &&
                        binding.emailField.error == null
                    ) {
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
                }
            }
        }
    }

}
