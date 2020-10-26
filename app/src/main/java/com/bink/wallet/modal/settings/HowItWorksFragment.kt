package com.bink.wallet.modal.settings

import android.os.Bundle
import com.bink.wallet.modal.generic.GenericModalFragment

class HowItWorksFragment : GenericModalFragment() {
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        arguments?.let { bundle ->
            setupUi(HowItWorksFragmentArgs.fromBundle(bundle).genericModalParameters)
        }
    }
}