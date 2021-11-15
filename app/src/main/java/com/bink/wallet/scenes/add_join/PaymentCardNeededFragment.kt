package com.bink.wallet.scenes.add_join

import android.content.Intent
import android.os.Bundle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bink.wallet.modal.generic.GenericModalFragment
import com.bink.wallet.utils.logPaymentCardSuccess
import com.bink.wallet.utils.navigateIfAdded
import com.bink.wallet.utils.requestCameraPermissionAndNavigate
import com.bink.wallet.utils.requestPermissionsResult
import com.bink.wallet.utils.scanResult

class PaymentCardNeededFragment : GenericModalFragment() {

    private val args by navArgs<PaymentCardNeededFragmentArgs>()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupUi(args.genericModalParameters)
    }

    override fun onFirstButtonClicked() {
        requestCameraPermissionAndNavigate(false, null)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        scanResult(
            requestCode,
            resultCode,
            data,
            { navigateToAddPaymentCard(it) },
            { logPaymentCardSuccess(it) })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        requestPermissionsResult(
            requestCode,
            permissions,
            grantResults,
            null,
            { navigateToAddPaymentCard() },
            null
        )
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun navigateToAddPaymentCard(cardNumber: String = "") {
        val direction =
            PaymentCardNeededFragmentDirections.actionPaymentCardNeededFragmentToAddPaymentCard(
                cardNumber
            )

        findNavController().navigateIfAdded(this, direction)
    }
}