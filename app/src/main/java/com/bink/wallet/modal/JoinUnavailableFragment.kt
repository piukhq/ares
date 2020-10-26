package com.bink.wallet.modal

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import com.bink.wallet.modal.generic.GenericModalFragment

/**
 */
class JoinUnavailableFragment : GenericModalFragment() {
    var link: String = ""
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        arguments?.let { bundle ->
            JoinUnavailableFragmentArgs.fromBundle(bundle).apply {
                setupUi(genericModalParameters)
                link = genericModalParameters.link
            }
        }
    }

    override fun onFirstButtonClicked() {
        if (link.isNotEmpty()) {
            findNavController().navigate(
                JoinUnavailableFragmentDirections.actionJoinUnavailableToBinkWebFragment(link)
            )
        }
    }
}