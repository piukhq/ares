package com.bink.wallet.modal.points_module

import android.os.Bundle
import com.bink.wallet.modal.generic.GenericModalFragment


class LCDErrorFragment :
    GenericModalFragment() {

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        arguments?.let {
            arguments?.let { bundle ->
                LCDErrorFragmentArgs.fromBundle(bundle).apply {
                    setupUi(this.genericModalParameters)
                }
            }
        }
    }
}
