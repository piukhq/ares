package com.bink.wallet.scenes.sign_up

import android.graphics.Rect
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import android.util.Patterns
import android.view.View
import android.view.ViewTreeObserver
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.navigation.fragment.findNavController
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.SignUpFragmentBinding
import com.bink.wallet.model.request.MarketingOption
import com.bink.wallet.model.request.SignUpRequest
import com.bink.wallet.utils.*
import com.bink.wallet.utils.FirebaseUtils.REGISTER_VIEW
import com.bink.wallet.utils.FirebaseUtils.getFirebaseIdentifier
import com.bink.wallet.utils.UtilFunctions.isNetworkAvailable
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        logScreenView(REGISTER_VIEW)
    }

    private val listener: ViewTreeObserver.OnGlobalLayoutListener =
        ViewTreeObserver.OnGlobalLayoutListener {
            val rec = Rect()
            binding.container.getWindowVisibleDisplayFrame(rec)
            val screenHeight = binding.container.rootView.height
            val keypadHeight = screenHeight - rec.bottom
            if (keypadHeight <= screenHeight * 0.15) {
                validateCredentials()
            }
        }


    private fun checkPasswordsMatch() =
        if (viewModel.password.value != viewModel.confirmPassword.value && !viewModel.confirmPassword.value.isNullOrEmpty()) {
            binding.confirmPasswordField.error = getString(R.string.password_not_match)
        } else {
            binding.confirmPasswordField.error = null
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
            }

            password.observeNonNull(this@SignUpFragment) {
                checkPasswordsMatch()
                requireContext().validatePassword(it, binding.passwordField)
            }

            confirmPassword.observeNonNull(this@SignUpFragment) {
                checkPasswordsMatch()
            }

            privacyPolicy.observeNonNull(this@SignUpFragment) {
            }

            termsCondition.observeNonNull(this@SignUpFragment) {
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
                if (!UtilFunctions.hasCertificatePinningFailed(it, requireContext())) {
                    requireContext().displayModalPopup(
                        EMPTY_STRING,
                        getString(R.string.registration_failed_text)
                    )
                }
            }

            signUpResponse.observeNonNull(this@SignUpFragment) {
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

                    viewModel.getMembershipPlans()
                }
            }
        }

        binding.progressSpinner.setOnTouchListener { _, _ -> true }

        binding.signUpButton.setOnClickListener {
            if (isNetworkAvailable(requireActivity(), true)) {
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

            logEvent(getFirebaseIdentifier(REGISTER_VIEW, binding.signUpButton.text.toString()))
        }

        initMembershipPlansObserver()
    }

    override fun onPause() {
        super.onPause()
        binding.container.viewTreeObserver.removeOnGlobalLayoutListener(listener)
    }

    private fun validateCredentials() {
        viewModel.email.value?.let {
            if (it.isNotEmpty()) {
                binding.emailField.error =
                    if (!Patterns.EMAIL_ADDRESS.matcher(it).matches()) {
                        getString(R.string.invalid_email_format)
                    } else {
                        null
                    }
            }
        }

        viewModel.password.value?.let {
            if (it.isNotEmpty()) {
                binding.passwordField.error =
                    if (!UtilFunctions.isValidField(PASSWORD_REGEX, it)) {
                        getString(R.string.password_description)
                    } else {
                        null
                    }
            }
        }

        viewModel.confirmPassword.value?.let {
            if (it.isNotEmpty()) {
                binding.confirmPasswordField.error =
                    if (it != viewModel.password.value) {
                        getString(R.string.password_not_match)
                    } else {
                        null
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

    private fun initMembershipPlansObserver() {
        viewModel.membershipPlanMutableLiveData.observeNonNull(this@SignUpFragment) {
            viewModel.isLoading.value = false
            finaliseAuthenticationFlow()
        }

        viewModel.membershipPlanErrorLiveData.observeNonNull(this@SignUpFragment) {
            finaliseAuthenticationFlow()
        }
    }

    private fun finaliseAuthenticationFlow() {
        findNavController().navigateIfAdded(
            this@SignUpFragment,
            R.id.global_to_home
        )
    }

}
