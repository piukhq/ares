package com.bink.wallet.modal

import android.os.Bundle
import com.bink.wallet.modal.generic.GenericModalFragment

class SecurityAndPrivacyFragment : GenericModalFragment() {

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        arguments?.let { bundle ->
            setupUi(SecurityAndPrivacyArgs.fromBundle(bundle).genericModalParameters)
        }
    }

    override fun onFirstButtonClicked() {
        super.onFirstButtonClicked()
        requireActivity().onBackPressed()
    }
}