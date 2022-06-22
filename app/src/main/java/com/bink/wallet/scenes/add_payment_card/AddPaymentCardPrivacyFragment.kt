package com.bink.wallet.scenes.add_payment_card

import android.os.Bundle
import android.view.View
import com.bink.wallet.modal.generic.GenericModalFragment

class AddPaymentCardPrivacyFragment : GenericModalFragment() {
     override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let { bundle ->
            setupUi(AddPaymentCardPrivacyFragmentArgs.fromBundle(bundle).genericModalParameters)
        }
        binding.firstButton.visibility = View.GONE
    }
}