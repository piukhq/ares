package com.bink.wallet.modal.terms_and_conditions

import android.os.Bundle
import android.view.View
import com.bink.wallet.R
import com.bink.wallet.modal.generic.GenericModalFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 */
class TermsAndConditionsFragment : GenericModalFragment() {
    override val viewModel: TermsAndConditionsViewModel by viewModel()
     override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let { bundle ->
            TermsAndConditionsFragmentArgs.fromBundle(bundle).apply {
                setupUi(genericModalParameters)
            }
        }
    }

    override fun onSecondButtonClicked() {
        super.onSecondButtonClicked()
        viewModel.destinationLiveData.value = R.id.global_to_home
    }
}