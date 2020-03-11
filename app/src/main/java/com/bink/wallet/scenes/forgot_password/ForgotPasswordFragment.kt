package com.bink.wallet.scenes.forgot_password

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.ForgotPasswordFragmentBinding
import com.bink.wallet.utils.*
import com.bink.wallet.utils.UtilFunctions.isNetworkAvailable
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

    override val layoutRes: Int = R.layout.forgot_password_fragment

    override val viewModel: ForgotPasswordViewModel by viewModel()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.viewModel = viewModel

        viewModel.email.observeNonNull(this) {
            requireContext().validateEmail(it, binding.emailText)
            setLoginButtonEnableStatus()
        }

        binding.buttonContinueEmail.setOnClickListener {
            if (isNetworkAvailable(requireActivity(), true)) {
                requireContext().validateEmail(viewModel.email.value, binding.emailText)
                if (binding.emailText.error != null) {
                    requireContext().displayModalPopup(
                        EMPTY_STRING,
                        getString(R.string.invalid_email_text)
                    )
                } else {
                    viewModel.isLoading.value = true
                    viewModel.forgotPassword()
                }
            }
        }

        viewModel.isLoading.observeNonNull(this@ForgotPasswordFragment) {
            with(binding) {
                progressSpinner.visibility = when (it) {
                    true -> View.VISIBLE
                    else -> View.GONE
                }

                buttonContinueEmail.isEnabled = !it
            }
        }

        viewModel.forgotPasswordResponse.observeNonNull(this) {
            viewModel.isLoading.value = false
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

        viewModel.forgotPasswordError.observeErrorNonNull(requireContext(), this) {
            viewModel.isLoading.value = false
        }
    }

    private fun setLoginButtonEnableStatus() {
        with(binding) {
            viewModel?.let {
                buttonContinueEmail.isEnabled =
                    (binding.emailText.error == null &&
                            (it.email.value ?: EMPTY_STRING).isNotBlank())
            }
        }
    }
}
