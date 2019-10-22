package com.bink.wallet.modal.loyalty_card_details

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import com.bink.wallet.R
import com.bink.wallet.modal.generic.GenericModalFragment

class SecurityAndPrivacyFragment : GenericModalFragment() {

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        arguments?.let { bundle ->
            setupUi(SecurityAndPrivacyFragmentArgs.fromBundle(bundle).genericModalParameters)
            val stringToSpan = binding.description.text
            val spannableString = SpannableStringBuilder(stringToSpan)
            val url = getString(R.string.terms_and_conditions_url)
            val hyperlinkText = getString(R.string.hyperlink_text)
            spannableString.setSpan(
                URLSpan(url),
                stringToSpan.indexOf(hyperlinkText),
                stringToSpan.indexOf(hyperlinkText) + hyperlinkText.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            binding.description.text = spannableString
            binding.description.linksClickable = true
            binding.description.movementMethod = LinkMovementMethod.getInstance()
        }
    }

    override fun onFirstButtonClicked() {
        super.onFirstButtonClicked()
        requireActivity().onBackPressed()
    }
}