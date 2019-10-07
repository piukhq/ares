package com.bink.wallet.modal.loyalty_card_details

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
        requireActivity().onBackPressed()
    }
}