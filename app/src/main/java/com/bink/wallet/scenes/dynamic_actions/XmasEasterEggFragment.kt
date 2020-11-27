package com.bink.wallet.scenes.dynamic_actions

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import com.bink.wallet.modal.BrandHeaderFragmentDirections
import com.bink.wallet.modal.generic.GenericModalFragment

class XmasEasterEggFragment : GenericModalFragment() {

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        arguments?.let { bundle ->
            XmasEasterEggFragmentArgs.fromBundle(bundle).apply {
                setupUi(genericModalParameters)
            }
        }
    }

    override fun onFirstButtonClicked() {
        //Launch Zendesk
    }
}