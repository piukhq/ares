package com.bink.wallet.scenes

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import android.util.Patterns
import android.view.View
import android.widget.CheckBox
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

    private fun validateEmail() =
        if (!Patterns.EMAIL_ADDRESS.matcher(viewModel.email.value ?: EMPTY_STRING).matches()) {
            binding.emailField.error = getString(R.string.incorrect_email_text)
        } else {
            binding.emailField.error = null
        }

    private fun validatePassword() = if (!UtilFunctions.isValidField(
            PASSWORD_REGEX,
            viewModel.password.value ?: EMPTY_STRING
        )
    ) {
        binding.passwordField.error =
            getString(R.string.password_description)
    } else {
        binding.passwordField.error = null
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
            signUpFooterMessage.text = HtmlCompat.fromHtml(
                getString(R.string.sign_up_footer_text),
                HtmlCompat.FROM_HTML_MODE_LEGACY
            )
            viewModel = this@SignUpFragment.viewModel

            buildHyperlinkSpanString(
                binding.checkboxTermsConditions.text.toString(),
                getString(R.string.terms_conds_text),
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
            email.observeNonNull(this@SignUpFragment) {
                validateEmail()
            }

            password.observeNonNull(this@SignUpFragment) {
                validatePassword()
            }

            confirmPassword.observeNonNull(this@SignUpFragment) {
                checkPasswordsMatch()
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
                    CredentialsUtils.createNewKey()

                    LocalStoreUtils.setAppSharedPref(
                        LocalStoreUtils.KEY_TOKEN,
                        CredentialsUtils.encrypt(getString(R.string.token_api_v1, it.api_key)),
                        requireContext()
                    )

                    LocalStoreUtils.setAppSharedPref(
                        LocalStoreUtils.KEY_EMAIL,
                        CredentialsUtils.encrypt(it.email ?: EMPTY_STRING),
                        requireContext()
                    )

                    marketingPref(
                        MarketingOption(
                            when (marketingMessages.value) {
                                true -> 1
                                else -> 0
                            }
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

            validateEmail()
            validatePassword()
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
                            "${getString(R.string.terms_conds_text)} & ${getString(R.string.privacy_policy_text)}"
                        )
                    } else {
                        if (termsCondition.value != true) {
                            getString(
                                R.string.accept_tc_pp,
                                getString(R.string.terms_conds_text)
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
        textView: CheckBox
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
