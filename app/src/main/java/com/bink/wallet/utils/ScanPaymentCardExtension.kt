package com.bink.wallet.utils

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bink.wallet.BuildConfig
import com.bink.wallet.data.SharedPreferenceManager
import com.bink.wallet.ui.factory.DialogFactory
import com.bink.wallet.utils.enums.BuildTypes
import com.getbouncer.cardscan.ui.CardScanActivity
import com.getbouncer.cardscan.ui.CardScanActivityResult
import com.getbouncer.cardscan.ui.CardScanActivityResultHandler

fun Fragment.requestCameraPermissionAndNavigate(
    requestPermissionLauncher: ActivityResultLauncher<String>?,
    shouldNavigateToScanLoyaltyCard: Boolean,
    navigateToScanLoyaltyCard: (() -> Unit)?,
    navigateToAddPaymentCard: (() -> Unit)?,
    navigateToBrowseBrands: (() -> Unit)?
) {

    SharedPreferenceManager.didAttemptToAddPaymentCard = !shouldNavigateToScanLoyaltyCard

    when {
        ContextCompat.checkSelfPermission(
            this.requireActivity(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED -> {
            // You can use the API that requires the permission.
            if (!shouldNavigateToScanLoyaltyCard) {
                openScanPaymentCard()
            } else {
                navigateToScanLoyaltyCard?.invoke()
            }

        }
        ActivityCompat.shouldShowRequestPermissionRationale(
            this.requireActivity(),
            Manifest.permission.CAMERA
        ) -> {
            // In an educational UI, explain to the user why your app requires this
            // permission for a specific feature to behave as expected. In this UI,
            // include a "cancel" or "no thanks" button that allows the user to
            // continue using your app without granting the permission.
            onRequestDenied(navigateToAddPaymentCard, navigateToBrowseBrands)

        }
        else -> {
            // You can directly ask for the permission.
            // The registered ActivityResultCallback gets the result of this request.

            requestPermissionLauncher?.launch(
                Manifest.permission.CAMERA
            )
        }
    }
}

fun Fragment.onRequestDenied(
    navigateToAddPaymentCard: (() -> Unit)?,
    navigateToBrowseBrands: (() -> Unit)?
) {
    DialogFactory.showPermissionsSettingsDialog(requireActivity()) {
        if (SharedPreferenceManager.didAttemptToAddPaymentCard) {
            navigateToAddPaymentCard?.invoke()
        } else {
            navigateToBrowseBrands?.invoke()
        }
    }
}

fun Fragment.requestPermissionsResult(
    navigateToScanLoyaltyCard: (() -> Unit)?,
    navigateToAddPaymentCard: (() -> Unit)?,
    navigateToBrowseBrands: (() -> Unit)?,
    isPermissionGranted: Boolean
) {
    if (isPermissionGranted) {
        if (SharedPreferenceManager.didAttemptToAddPaymentCard) {
            openScanPaymentCard()
        } else {
            navigateToScanLoyaltyCard?.invoke()
        }
    } else {
        val shouldShowPermissionExplanation =
            ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.CAMERA
            )

        if (!shouldShowPermissionExplanation) {
            DialogFactory.showPermissionsSettingsDialog(requireActivity()) {
                if (SharedPreferenceManager.didAttemptToAddPaymentCard) {
                    navigateToAddPaymentCard?.invoke()
                } else {
                    navigateToBrowseBrands?.invoke()
                }
            }
        }
    }
}


fun Fragment.scanResult(
    requestCode: Int,
    resultCode: Int,
    data: Intent?,
    navigateToAddPaymentCard: (String) -> Unit,
    logPaymentCardSuccess: (Boolean) -> Unit
) {
    if (CardScanActivity.isScanResult(requestCode)) {
        val cardActivityResultHandler = object : CardScanActivityResultHandler {
            override fun analyzerFailure(scanId: String?) {
                logPaymentCardSuccess(false)
                DialogFactory.showTryAgainGenericError(requireActivity())
            }

            override fun cameraError(scanId: String?) {
            }

            override fun canceledUnknown(scanId: String?) {
                logPaymentCardSuccess(false)
                DialogFactory.showTryAgainGenericError(requireActivity())
            }

            override fun cardScanned(scanId: String?, scanResult: CardScanActivityResult) {
                scanResult.pan?.let { safeCardNumber ->
                    logPaymentCardSuccess(true)
                    navigateToAddPaymentCard(safeCardNumber)
                }
            }

            override fun enterManually(scanId: String?) {
                navigateToAddPaymentCard("")
            }

            override fun userCanceled(scanId: String?) {

            }

        }

        CardScanActivity.parseScanResult(resultCode, data, cardActivityResultHandler)
    }

}


fun Fragment.openScanPaymentCard() {
    val bouncerKey = LocalStoreUtils.getAppSharedPref(
        LocalStoreUtils.KEY_BOUNCER_KEY
    )
    shouldShowLogo()
    if (bouncerKey != null) {
        CardScanActivity.start(
            this,
            bouncerKey,
            enableEnterCardManually = true,
            enableExpiryExtraction = false,
            enableNameExtraction = false
        )
    }

}


fun Fragment.logPaymentCardSuccess(wasSuccess: Boolean) {
    val map = HashMap<String, String>()
    map[FirebaseEvents.PAYMENT_CARD_SCAN_SUCCESS] = if (wasSuccess) "1" else "0"
    logEvent(FirebaseEvents.PAYMENT_CARD_SCAN, map)
}

fun Fragment.logEvent(name: String, parameters: Map<String, String>) {
    if (BuildConfig.BUILD_TYPE.lowercase() == BuildTypes.RELEASE.type) {
        val bundle = Bundle()

        for (entry: Map.Entry<String, String> in parameters) {
            bundle.putString(entry.key, entry.value)
        }

        getMainActivity().firebaseAnalytics.logEvent(name, bundle)
    }
}

private fun shouldShowLogo(shouldShow: Boolean = false) {
    com.getbouncer.scan.framework.Config.displayLogo = shouldShow
}