package com.bink.wallet.scenes.registration

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.StyleSpan
import android.text.style.URLSpan
import android.view.View
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.AcceptTcFragmentBinding
import com.bink.wallet.model.auth.FacebookAuthRequest
import com.bink.wallet.model.request.MarketingOption
import com.bink.wallet.utils.LocalStoreUtils
import com.bink.wallet.utils.displayModalPopup
import com.bink.wallet.utils.navigateIfAdded
import com.bink.wallet.utils.observeNonNull
import com.bink.wallet.utils.toolbar.FragmentToolbar
import com.facebook.AccessToken
import kotlinx.coroutines.runBlocking
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

class AcceptTCFragment : BaseFragment<AcceptTCViewModel, AcceptTcFragmentBinding>() {
    override val layoutRes: Int
        get() = R.layout.accept_tc_fragment
    override val viewModel: AcceptTCViewModel by viewModel()

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .with(binding.toolbar)
            .build()
    }

    private lateinit var termsAndConditionsHyperlink: String
    private lateinit var privacyPolicyHyperlink: String
    private lateinit var boldedTexts: Array<String>
    private var userEmail: String? = null
    private var accessToken: AccessToken? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        termsAndConditionsHyperlink = getString(R.string.terms_conditions_text)
        privacyPolicyHyperlink = getString(R.string.privacy_policy_text)
        boldedTexts = resources.getStringArray(R.array.terms_bold_text_array)

        arguments?.let {
            with(AcceptTCFragmentArgs.fromBundle(it)) {
                userEmail = email
                this@AcceptTCFragment.accessToken = accessToken
            }
        }

        buildHyperlinkSpanString(
            binding.acceptTcDisclaimer.text.toString(),
            termsAndConditionsHyperlink,
            getString(R.string.terms_and_conditions_url),
            binding.acceptTcDisclaimer
        )

        buildHyperlinkSpanString(
            binding.acceptPrivacyPolicyDisclaimer.text.toString(),
            privacyPolicyHyperlink,
            getString(R.string.privacy_policy_url),
            binding.acceptPrivacyPolicyDisclaimer
        )

        buildDescriptionSpanString()

        viewModel.facebookAuthError.observeNonNull(this) {
            binding.accept.isClickable = false
            val timer = Timer()
            context?.resources?.getInteger(R.integer.button_disabled_delay)?.toLong()
                ?.let { delay ->
                    timer.schedule(object : TimerTask() {
                        override fun run() {
                            binding.accept.isClickable = true
                        }
                    }, delay)
                }
            requireContext().displayModalPopup(getString(R.string.facebook_failed), null)
        }

        binding.acceptTc.setOnCheckedChangeListener { _, isChecked ->
            viewModel.shouldAcceptBeEnabledTC.value = isChecked
        }

        binding.acceptPrivacyPolicy.setOnCheckedChangeListener { _, isChecked ->
            viewModel.shouldAcceptBeEnabledPrivacy.value = isChecked
        }

        viewModel.shouldAcceptBeEnabledTC.observeNonNull(this) { enabledTc ->
            viewModel.shouldAcceptBeEnabledPrivacy.value?.let {
                binding.accept.isEnabled = enabledTc == true &&
                        it == true
            }
        }

        viewModel.shouldAcceptBeEnabledPrivacy.observeNonNull(this) { enabledPrivacy ->
            viewModel.shouldAcceptBeEnabledTC.value?.let {
                binding.accept.isEnabled = enabledPrivacy == true &&
                        it == true
            }
        }

        viewModel.facebookAuthResult.observeNonNull(this) {
            runBlocking {
                LocalStoreUtils.setAppSharedPref(
                    LocalStoreUtils.KEY_TOKEN,
                    getString(R.string.token_api_v1, it.api_key)
                )
            }
            if (binding.acceptMarketing.isChecked) {
                viewModel.handleMarketingPreferences(
                    MarketingOption(1)
                )
            }
            findNavController().navigateIfAdded(this, R.id.accept_to_lcd)
        }

        binding.accept.setOnClickListener {
            if (accessToken?.token != null &&
                userEmail != null &&
                accessToken?.userId != null
            )
                viewModel.authWithFacebook(
                    FacebookAuthRequest(
                        accessToken?.token!!,
                        userEmail!!,
                        accessToken?.userId!!
                    )
                )
        }
        binding.decline.setOnClickListener {
            findNavController().navigateIfAdded(this, R.id.accept_to_onboarding)
        }

        binding.back.setOnClickListener {
            findNavController().navigateIfAdded(this, R.id.accept_to_onboarding)
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
            spannableString.indexOf(stringToHyperlink) - 1,
            spannableString.indexOf(stringToHyperlink) + stringToHyperlink.length,
            Spanned.SPAN_INCLUSIVE_EXCLUSIVE
        )
        textView.text = spannableString
        textView.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun buildDescriptionSpanString() {
        val spannableString = SpannableString(binding.overallDisclaimer.text)
        boldedTexts.forEach { string ->
            spannableString.setSpan(
                StyleSpan(android.graphics.Typeface.BOLD),
                spannableString.indexOf(string),
                spannableString.indexOf(string) + string.length,
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE
            )
        }
        binding.overallDisclaimer.text = spannableString
    }
}