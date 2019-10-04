package com.bink.wallet.modal

import android.os.Bundle
import com.bink.wallet.modal.generic.GenericModalFragment

class AboutMembershipFragment: GenericModalFragment() {

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        arguments?.let { bundle ->
            setupUi(AboutMembershipFragmentArgs.fromBundle(bundle).genericModalParameters)
        }
    }

    override fun onFirstButtonClicked() {
        activity?.onBackPressed()
    }
}