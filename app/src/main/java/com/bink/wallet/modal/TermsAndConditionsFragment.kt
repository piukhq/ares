package com.bink.wallet.modal

import android.os.Bundle
import com.bink.wallet.modal.generic.GenericModalFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 */
class TermsAndConditionsFragment : GenericModalFragment() {
    override val viewModel: TermsAndConditionsViewModel by viewModel()
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        arguments?.let { bundle ->
            TermsAndConditionsFragmentArgs.fromBundle(bundle).apply {
                setupUi(this.genericModalParameters)
            }
        }
    }
}