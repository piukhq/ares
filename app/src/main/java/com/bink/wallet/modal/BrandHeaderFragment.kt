package com.bink.wallet.modal

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import com.bink.wallet.modal.generic.GenericModalFragment

class BrandHeaderFragment : GenericModalFragment() {

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        arguments?.let { bundle ->
            JoinUnavailableFragmentArgs.fromBundle(bundle).apply {
                setupUi(genericModalParameters)
            }
        }
    }

    override fun onFirstButtonClicked() {
        findNavController().popBackStack()
    }
}