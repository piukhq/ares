package com.bink.wallet.scenes

import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.util.Patterns
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.SignUpFragmentBinding
import com.bink.wallet.model.request.SignUpRequest
import com.bink.wallet.utils.UtilFunctions
import com.bink.wallet.utils.displayModalPopup
import com.bink.wallet.utils.observeNonNull
import com.bink.wallet.utils.toolbar.FragmentToolbar
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

        val sb =
            SpannableStringBuilder(getString(R.string.sign_up_footer_text))

        val bss = StyleSpan(Typeface.BOLD)
        sb.setSpan(bss, 50, 80, Spannable.SPAN_INCLUSIVE_INCLUSIVE)

        with(binding) {
            signUpFooterMessage.text = sb
            viewModel = this@SignUpFragment.viewModel
        }

        with(viewModel) {
            email.observeNonNull(this@SignUpFragment) {
                if (!Patterns.EMAIL_ADDRESS.matcher(it).matches()) {
                    binding.emailField.error = "Incorrect email"
                } else {
                    binding.emailField.error = null
                }
            }

            password.observeNonNull(this@SignUpFragment) {
                if (!UtilFunctions.isValidField("^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{8,30}$", it)) {
                    binding.passwordField.error =
                        "Password should be 8 or more characters, with at least 1 uppercase. 1 lowercase and a number"
                } else {
                    binding.passwordField.error = null
                }
            }

            confirmPassword.observeNonNull(this@SignUpFragment) {
                if (password.value != it) {
                    binding.confirmPasswordField.error = "Password do not match"
                } else {
                    binding.confirmPasswordField.error = null
                }
            }
        }

        binding.signUpButton.setOnClickListener {
            with(viewModel) {
                if (termsCondition.value == true && privacyPolicy.value == true) {
                    signUp(SignUpRequest(email = email.value, password = password.value))
                } else {
                    requireContext().displayModalPopup("", "Check T&C and Privacy Policy")
                }
            }
        }
    }

}
