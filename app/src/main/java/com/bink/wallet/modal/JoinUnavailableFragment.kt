package com.bink.wallet.modal

import android.content.Intent
import android.net.Uri
import android.os.Bundle
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
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
    }
}