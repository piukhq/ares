package com.bink.wallet.scenes.sign_up.continue_with_email

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import androidx.core.widget.doAfterTextChanged
import androidx.navigation.fragment.findNavController
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.ContinueWithEmailFragmentBinding
import com.bink.wallet.scenes.sign_up.SignUpFragmentDirections
import com.bink.wallet.utils.*
import com.bink.wallet.utils.toolbar.FragmentToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel

class ContinueWithEmailFragment : BaseFragment<ContinueWithEmailViewModel, ContinueWithEmailFragmentBinding>() {

    override val layoutRes = R.layout.continue_with_email_fragment

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .with(binding.toolbar)
            .shouldDisplayBack(requireActivity())
            .build()
    }

    override val viewModel: ContinueWithEmailViewModel by viewModel()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.magicLinkError.observeNonNull(this) {
            showMagicLinkFail()
        }

        viewModel.isSuccessful.observeNonNull(this){
            
        }

        viewModel.isLoading.observeNonNull(this) {
            with(binding) {
                progressSpinner.visibility = when (it) {
                    true -> {
                        binding.signUpButton.isEnabled = false
                        View.VISIBLE
                    }
                    else -> {
                        binding.signUpButton.isEnabled = true
                        View.GONE
                    }
                }
            }
        }

        binding.magicLinkText.setMagicLinkUrl(
            getString(R.string.magic_link_full),
            getString(R.string.magic_link),
            urlClickListener = { url ->
                findNavController().navigate(
                    SignUpFragmentDirections.globalToWeb(
                        url
                    )
                )
            }
        )

        binding.termsAndConditionsText.setTermsAndPrivacyUrls(
            getString(R.string.terms_and_conditions_message),
            getString(R.string.terms_and_conditions_title),
            getString(R.string.privacy_policy_text),
            urlClickListener = { url ->
                findNavController().navigate(
                    SignUpFragmentDirections.globalToWeb(
                        url
                    )
                )
            }
        )

        binding.checkboxTermsConditions.setOnCheckedChangeListener { _, isChecked ->
            binding.signUpButton.isEnabled = false
            validateCredentials(isChecked)
        }

        binding.emailField.doAfterTextChanged {
            validateCredentials(binding.checkboxTermsConditions.isChecked)
        }

        binding.signUpButton.setOnClickListener {
            binding.emailField.text.trim().toString().let { email ->
                viewModel.postMagicLink(email)
            }
        }

    }

    private fun validateCredentials(isTermsChecked: Boolean) {
        binding.emailField.text.trim().toString().let {
            if (it.isNotEmpty()) {
                binding.emailField.error =
                    if (!UtilFunctions.isValidField(EMAIL_REGEX, it)) {
                        binding.signUpButton.isEnabled = false
                        getString(R.string.invalid_email_format)
                    } else {
                        binding.signUpButton.isEnabled = isTermsChecked
                        null
                    }
            }
        }

    }

    private fun showMagicLinkFail() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.error)
            .setMessage(getString(R.string.error_magic_link_failed))
            .setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

}