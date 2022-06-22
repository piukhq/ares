package com.bink.wallet.modal.loyalty_card_details

import android.os.Bundle
import android.view.View
import com.bink.wallet.modal.generic.GenericModalFragment

class SecurityAndPrivacyFragment : GenericModalFragment() {
     override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let { bundle ->
            setupUi(SecurityAndPrivacyFragmentArgs.fromBundle(bundle).genericModalParameters)
        }
    }
}