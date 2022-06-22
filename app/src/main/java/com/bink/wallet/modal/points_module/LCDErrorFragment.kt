package com.bink.wallet.modal.points_module

import android.os.Bundle
import android.view.View
import com.bink.wallet.modal.generic.GenericModalFragment


class LCDErrorFragment : GenericModalFragment() {

     override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            arguments?.let { bundle ->
                LCDErrorFragmentArgs.fromBundle(bundle).apply {
                    setupUi(this.genericModalParameters)
                }
            }
        }
    }
}
