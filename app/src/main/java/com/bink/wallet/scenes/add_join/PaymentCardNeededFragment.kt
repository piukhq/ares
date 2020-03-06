package com.bink.wallet.scenes.add_join

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bink.wallet.modal.generic.GenericModalFragment

class PaymentCardNeededFragment : GenericModalFragment() {

    private val args by navArgs<PaymentCardNeededFragmentArgs>()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupUi(args.genericModalParameters)
    }

    override fun onFirstButtonClicked() {
        findNavController().navigate(
            PaymentCardNeededFragmentDirections
                .actionPaymentCardNeededFragmentToAddPaymentCard()
        )
    }
}