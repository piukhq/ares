package com.bink.wallet.modal

import android.os.Bundle
import com.bink.wallet.modal.generic.GenericModalFragment

class GhostRegistrationUnavailableFragment: GenericModalFragment() {

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        arguments?.let { bundle ->
            GhostRegistrationUnavailableFragmentArgs.fromBundle(bundle).apply {
                setupUi(genericModalParameters)
            }
        }
    }
}