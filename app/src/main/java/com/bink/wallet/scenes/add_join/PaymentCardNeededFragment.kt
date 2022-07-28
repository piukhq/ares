package com.bink.wallet.scenes.add_join

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bink.wallet.modal.generic.GenericModalFragment
import com.bink.wallet.utils.*

class PaymentCardNeededFragment : GenericModalFragment() {

    private val args by navArgs<PaymentCardNeededFragmentArgs>()

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. Continue the action or workflow in your
                // app.
                requestPermissionsResult(
                    null,
                    { navigateToAddPaymentCard() },
                    null,
                    isGranted
                )

            } else {
                // Explain to the user that the feature is unavailable because the
                // features requires a permission that the user has denied. At the
                // same time, respect the user's decision. Don't link to system
                // settings in an effort to convince the user to change their
                // decision.
            }
        }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupUi(args.genericModalParameters)
    }

    override fun onFirstButtonClicked() {
        requestCameraPermissionAndNavigate(
            false,
            null,
            requestPermissionLauncher,
            { navigateToAddPaymentCard() },
            null
        )
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
//        requestPermissionsResult(
//            requestCode,
//            permissions,
//            grantResults,
//            null,
//            { navigateToAddPaymentCard() },
//            null
//        )
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