package com.bink.wallet.scenes.forgot_password

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.ForgotPasswordFragmentBinding
import com.bink.wallet.utils.EMPTY_STRING
import com.bink.wallet.utils.displayModalPopup
import com.bink.wallet.utils.navigateIfAdded
import com.bink.wallet.utils.observeNonNull
import com.bink.wallet.utils.toolbar.FragmentToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel


class ForgotPasswordFragment :
    BaseFragment<ForgotPasswordViewModel, ForgotPasswordFragmentBinding>() {
    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .with(binding.toolbar)
            .shouldDisplayBack(requireActivity())
            .build()
    }

    private fun validateEmail() =
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(
                viewModel.email.value ?: EMPTY_STRING
            ).matches()
        ) {
            binding.emailText.error = getString(R.string.invalid_email_text)
        } else {
            binding.emailText.error = null
        }

    override val layoutRes: Int = R.layout.forgot_password_fragment

    override val viewModel: ForgotPasswordViewModel by viewModel()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.viewModel = viewModel

        viewModel.email.observeNonNull(this) {
            validateEmail()
        }

        binding.buttonContinueEmail.setOnClickListener {

            validateEmail()

            if (binding.emailText.error != null) {
                requireContext().displayModalPopup(
                    EMPTY_STRING,
                    getString(R.string.invalid_email_text)
                )
            } else {
                viewModel.forgotPassword()
            }
        }

        viewModel.forgotPasswordResponse.observeNonNull(this) {
            requireContext().displayModalPopup(
                getString(R.string.forgot_password_title),
                getString(R.string.forgot_password_dialog_description),
                okAction = {
                    findNavController().navigateIfAdded(
                        this,
                        R.id.forgot_password_to_onboarding
                    )
                }
            )
        }

        viewModel.forgotPasswordError.observeNonNull(this) {
            requireContext().displayModalPopup(
                EMPTY_STRING,
                getString(R.string.error_description)
            )
        }

    }

}
