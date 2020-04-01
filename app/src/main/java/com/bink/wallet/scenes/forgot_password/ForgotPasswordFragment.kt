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
    override val layoutRes: Int = R.layout.forgot_password_fragment
    override val viewModel: ForgotPasswordViewModel by viewModel()

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .with(binding.toolbar)
            .shouldDisplayBack(requireActivity())
            .build()
    }

    override fun onResume() {
        super.onResume()
        setupLayoutListener(binding.container, ::validateCredentials)
        registerLayoutListener(binding.container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.viewModel = viewModel

        viewModel.email.observeNonNull(this) {
            requireContext().validateEmail(it, binding.emailText)
        }

        binding.buttonContinue.setOnClickListener {
            if (isNetworkAvailable(requireActivity(), true)) {
                viewModel.forgotPassword()
            }
        }

        viewModel.isLoading.observeNonNull(this@ForgotPasswordFragment) {
            binding.progressSpinner.visibility = if (it) {
                View.VISIBLE
            } else {
                View.GONE
            }
            binding.buttonContinue.isEnabled = !it
        }

        viewModel.forgotPasswordResponse.observeNonNull(this) {
            viewModel.isLoading.value = false
            requireContext().displayModalPopup(
                getString(R.string.forgot_password_title),
                getString(R.string.forgot_password_dialog_description),
                okAction = {
                    findNavController().navigateUp()
                }
            )
        }

        viewModel.forgotPasswordError.observeErrorNonNull(requireContext(), this, true) {
            viewModel.isLoading.value = false
        }
    }

    override fun onPause() {
        super.onPause()
        removeLayoutListener(binding.container)
    }

    private fun validateCredentials() {
        viewModel.email.value?.let {
            if (it.isNotEmpty()) {
                binding.emailText.error =
                    if (!UtilFunctions.isValidField(EMAIL_REGEX, it)) {
                        getString(R.string.invalid_email_format)
                    } else {
                        null
                    }
            }
        }
    }
}
