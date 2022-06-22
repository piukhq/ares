package com.bink.wallet.modal

import android.os.Bundle
import android.view.View
import com.bink.wallet.modal.generic.GenericModalFragment

class GhostRegistrationUnavailableFragment: GenericModalFragment() {

     override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let { bundle ->
            GhostRegistrationUnavailableFragmentArgs.fromBundle(bundle).apply {
                setupUi(genericModalParameters)
            }
        }
    }
}