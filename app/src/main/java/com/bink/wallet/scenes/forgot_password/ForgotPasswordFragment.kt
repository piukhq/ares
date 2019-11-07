package com.bink.wallet.scenes.forgot_password

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.ForgotPasswordFragmentBinding
import com.bink.wallet.utils.*
import com.bink.wallet.utils.toolbar.FragmentToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel


class ForgotPasswordFragment(override val layoutRes: Int = R.layout.forgot_password_fragment) :
    BaseFragment<ForgotPasswordViewModel, ForgotPasswordFragmentBinding>() {
    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .with(binding.toolbar)
            .shouldDisplayBack(requireActivity())
            .build()
    }

    override val viewModel: ForgotPasswordViewModel by viewModel()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.viewModel = viewModel

        viewModel.email.observeNonNull(this) {
            if (!UtilFunctions.isValidField(
                    DEFAULT_EMAIL_REGEX,
                    it
                )
            )
                binding.emailText.error = getString(R.string.invalid_email_text)
            else
                binding.emailText.error = null
        }

        binding.buttonContinueEmail.setOnClickListener {
            if (!UtilFunctions.isValidField(
                    DEFAULT_EMAIL_REGEX,
                    viewModel.email.value
                )
            )
                requireContext().displayModalPopup(
                    EMPTY_STRING,
                    getString(R.string.invalid_email_text)
                )
            else
                viewModel.forgotPassword()
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

    }

}
