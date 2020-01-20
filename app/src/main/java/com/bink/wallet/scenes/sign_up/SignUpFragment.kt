package com.bink.wallet.scenes.sign_up

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import android.view.View
import android.widget.TextView
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

    private fun setSignupButtonEnableStatus() {
        with(viewModel) {
            termsCondition.value?.let { termsConditions ->
                privacyPolicy.value?.let { privacyPolicy ->
                    binding.signUpButton.isEnabled =
                        (binding.passwordField.error == null &&
                                binding.emailField.error == null &&
                                binding.confirmPasswordField.error == null &&
                                (email.value ?: EMPTY_STRING).isNotBlank() &&
                                (password.value ?: EMPTY_STRING).isNotBlank() &&
                                (confirmPassword.value ?: EMPTY_STRING).isNotBlank() &&
                                confirmPassword.value == password.value &&
                                termsConditions &&
                                privacyPolicy
                                )
                }
            }
        }
    }

    private fun checkPasswordsMatch() =
        if (viewModel.password.value != viewModel.confirmPassword.value) {
            binding.confirmPasswordField.error = getString(R.string.password_not_match)
        } else {
            binding.confirmPasswordField.error = null
        }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        with(binding) {
            signUpButton.isEnabled = false

            signUpFooterMessage.text = HtmlCompat.fromHtml(
                getString(R.string.sign_up_footer_text),
                HtmlCompat.FROM_HTML_MODE_LEGACY
            )
            viewModel = this@SignUpFragment.viewModel

            buildHyperlinkSpanString(
                binding.checkboxTermsConditions.text.toString(),
                getString(R.string.terms_conditions_text),
                getString(R.string.terms_and_conditions_url),
                binding.checkboxTermsConditions
            )

            buildHyperlinkSpanString(
                binding.checkboxPrivacyPolicy.text.toString(),
                getString(R.string.privacy_policy_text),
                getString(R.string.privacy_policy_url),
                binding.checkboxPrivacyPolicy
            )
        }

        with(viewModel) {
            privacyPolicy.value = false
            termsCondition.value = false
            marketingMessages.value = false

            email.observeNonNull(this@SignUpFragment) {
                requireContext().validateEmail(it, binding.emailField)
                setSignupButtonEnableStatus()
            }

            password.observeNonNull(this@SignUpFragment) {
                requireContext().validatePassword(it, binding.passwordField)
                setSignupButtonEnableStatus()
            }

            confirmPassword.observeNonNull(this@SignUpFragment) {
                checkPasswordsMatch()
                setSignupButtonEnableStatus()
            }

            privacyPolicy.observeNonNull(this@SignUpFragment) {
                setSignupButtonEnableStatus()
            }

            termsCondition.observeNonNull(this@SignUpFragment) {
                setSignupButtonEnableStatus()
            }

            isLoading.observeNonNull(this@SignUpFragment) {
                with(binding) {
                    progressSpinner.visibility = when (it) {
                        true -> View.VISIBLE
                        else -> View.GONE
                    }

                    signUpButton.isEnabled = !it
                }
            }

            signUpErrorResponse.observeNonNull(this@SignUpFragment) {
                isLoading.value = false
                requireContext().displayModalPopup(
                    EMPTY_STRING,
                    getString(R.string.registration_failed_text)
                )
            }

            signUpResponse.observeNonNull(this@SignUpFragment) {
                isLoading.value = false
                runBlocking {
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

                    findNavController().navigateIfAdded(
                        this@SignUpFragment,
                        R.id.global_to_home
                    )
                }
            }
        }

        binding.progressSpinner.setOnTouchListener { _, _ -> true }

        binding.signUpButton.setOnClickListener {

            requireContext().validateEmail(viewModel.email.value, binding.emailField)
            requireContext().validatePassword(viewModel.password.value, binding.passwordField)
            checkPasswordsMatch()

            with(viewModel) {
                if (termsCondition.value == true &&
                    privacyPolicy.value == true
                ) {
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
                    val dialogDescription = if (termsCondition.value != true &&
                        privacyPolicy.value != true
                    ) {
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
    }

    private fun buildHyperlinkSpanString(
        stringToSpan: String,
        stringToHyperlink: String,
        url: String,
        textView: TextView
    ) {
        val spannableString = SpannableString(stringToSpan)
        spannableString.setSpan(
            URLSpan(url),
            spannableString.indexOf(stringToHyperlink),
            spannableString.indexOf(stringToHyperlink) + stringToHyperlink.length,
            Spanned.SPAN_INCLUSIVE_EXCLUSIVE
        )
        textView.text = spannableString
        textView.movementMethod = LinkMovementMethod.getInstance()
    }

}
