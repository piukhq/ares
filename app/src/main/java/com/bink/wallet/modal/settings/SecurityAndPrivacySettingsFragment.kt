package com.bink.wallet.modal.settings

import android.os.Bundle
import com.bink.wallet.modal.generic.GenericModalFragment
import com.bink.wallet.modal.loyalty_card_details.SecurityAndPrivacyFragmentArgs

class SecurityAndPrivacySettingsFragment : GenericModalFragment() {
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        arguments?.let { bundle ->
            setupUi(SecurityAndPrivacyFragmentArgs.fromBundle(bundle).genericModalParameters)
        }
    }
}