package com.bink.wallet.modal.settings

import android.os.Bundle
import android.view.View
import com.bink.wallet.modal.generic.GenericModalFragment

class HowItWorksFragment : GenericModalFragment() {
     override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let { bundle ->
            setupUi(HowItWorksFragmentArgs.fromBundle(bundle).genericModalParameters)
        }
    }
}