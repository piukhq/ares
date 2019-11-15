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
import com.bink.wallet.utils.displayModalPopup
import com.bink.wallet.utils.observeNonNull
import com.bink.wallet.utils.toolbar.FragmentToolbar
import com.facebook.AccessToken
import org.koin.androidx.viewmodel.ext.android.viewModel

class AcceptTCFragment : BaseFragment<AcceptTCViewModel, AcceptTcFragmentBinding>() {
    override val layoutRes: Int
        get() = R.layout.accept_tc_fragment
    override val viewModel: AcceptTCViewModel by viewModel()

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .with(binding.toolbar)
            .shouldDisplayBack(requireActivity())
            .build()
    }

    private val termsAndConditionsHyperlink = "Terms and Conditions"
    private val privacyPolicyHyperlink = "Privacy Policy"
    private val boldedTexts = arrayListOf("rewards", "offers", "updates")
    private var userEmail: String? = null
    private var accessToken: AccessToken? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

        viewModel.facebookAuthError.observeNonNull(this){
            requireContext().displayModalPopup(getString(R.string.facebook_failed), null)
        }

        binding.acceptTc.setOnCheckedChangeListener { _, isChecked ->
            binding.accept.isEnabled = isChecked
        }

        binding.accept.setOnClickListener {
            viewModel.authWithFacebook(FacebookAuthRequest(accessToken?.token!!, userEmail!!, accessToken?.userId!!))
        }
        binding.decline.setOnClickListener {
            findNavController().popBackStack()
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