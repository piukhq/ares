package com.bink.wallet.scenes.forgot_password

import android.graphics.Rect
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.view.ViewTreeObserver
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

    private val listener: ViewTreeObserver.OnGlobalLayoutListener =
        ViewTreeObserver.OnGlobalLayoutListener {
            val rec = Rect()
            binding.container.getWindowVisibleDisplayFrame(rec)
            val screenHeight = binding.container.rootView.height
            val keypadHeight = screenHeight - rec.bottom
            if (keypadHeight <= screenHeight * KEYBOARD_TO_SCREEN_HEIGHT_RATIO) {
                validateCredentials()
            }
        }

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .with(binding.toolbar)
            .shouldDisplayBack(requireActivity())
            .build()
    }

    override fun onResume() {
        super.onResume()
        binding.container.viewTreeObserver.addOnGlobalLayoutListener(listener)
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
                    findNavController().navigateIfAdded(
                        this,
                        R.id.forgot_password_to_onboarding
                    )
                }
            )
        }

        viewModel.forgotPasswordError.observeNonNull(this) {
            viewModel.isLoading.value = false
            if (!UtilFunctions.hasCertificatePinningFailed(it, requireContext())) {
                requireContext().displayModalPopup(
                    EMPTY_STRING,
                    getString(R.string.error_description)
                )
            }
        }
    }

    override fun onPause() {
        super.onPause()
        binding.container.viewTreeObserver.removeOnGlobalLayoutListener(listener)
    }

    private fun validateCredentials() {
        viewModel.email.value?.let {
            if (it.isNotEmpty()) {
                binding.emailText.error =
                    if (!Patterns.EMAIL_ADDRESS.matcher(it).matches()) {
                        getString(R.string.invalid_email_format)
                    } else {
                        null
                    }
            }
        }
    }
}
