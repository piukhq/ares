package com.bink.wallet.modal.loyalty_card_details

import android.os.Bundle
import com.bink.wallet.modal.generic.GenericModalFragment

class SecurityAndPrivacyFragment : GenericModalFragment() {

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        arguments?.let { bundle ->
            setupUi(SecurityAndPrivacyFragmentArgs.fromBundle(bundle).genericModalParameters)
        }
    }

    override fun onFirstButtonClicked() {
        super.onFirstButtonClicked()
        requireActivity().onBackPressed()
    }
}