package com.bink.wallet.utils

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bink.wallet.BuildConfig
import com.bink.wallet.MainActivity
import com.bink.wallet.data.SharedPreferenceManager
import com.bink.wallet.ui.factory.DialogFactory
import com.bink.wallet.utils.enums.BuildTypes
import com.getbouncer.cardscan.ui.CardScanActivity
import com.getbouncer.cardscan.ui.CardScanActivityResult
import com.getbouncer.cardscan.ui.CardScanActivityResultHandler
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.*
import kotlin.collections.HashMap

fun Fragment.requestCameraPermissionAndNavigate(
    shouldNavigateToScanLoyaltyCard: Boolean,
    navigateToScanLoyaltyCard: (() -> Unit)?
) {
    SharedPreferenceManager.didAttemptToAddPaymentCard = !shouldNavigateToScanLoyaltyCard
    val permission = activity?.let {
        ContextCompat.checkSelfPermission(
            it,
            Manifest.permission.CAMERA
        )
    }

    if (permission != PackageManager.PERMISSION_GRANTED) {
        requestPermissions(
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_REQUEST_CODE
        )
    } else {
        if (shouldNavigateToScanLoyaltyCard) {
            navigateToScanLoyaltyCard?.invoke()
        } else {
            openScanPaymentCard()
        }
    }
}

fun Fragment.requestPermissionsResult(
    requestCode: Int,
    permissions: Array<out String>,
    grantResults: IntArray,
    activityResult: ActivityResultLauncher<String>?,
    navigateToScanLoyaltyCard: (() -> Unit)?,
    navigateToAddPaymentCard: (() -> Unit)?,
    navigateToBrowseBrands: (() -> Unit)?
) {
    if (requestCode == CAMERA_REQUEST_CODE) {
        if ((permissions[0] == Manifest.permission.CAMERA)
            && (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        ) {
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

                DialogFactory.showPermissionsSettingsDialog(requireActivity(), activityResult) {
                    if (SharedPreferenceManager.didAttemptToAddPaymentCard) {
                        navigateToAddPaymentCard?.invoke()
                    } else {
                        navigateToBrowseBrands?.invoke()
                    }
                }

            }
        }
    }

}

fun Fragment.handleGalleryResult(uri: Uri?, callback: (String?) -> Unit){
    if (uri != null){
        val inputImage = InputImage.fromFilePath(requireContext(), uri)
        val scanner = BarcodeScanning.getClient()

        scanner.process(inputImage)
            .addOnSuccessListener {
                if(it.isNullOrEmpty()) {
                    callback(null)
                } else {
                    callback(it[0].displayValue)
                }
            }
            .addOnFailureListener {
                callback(null)
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
         val cardActivityResultHandler =  object :CardScanActivityResultHandler{
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

        CardScanActivity.parseScanResult(resultCode,data, cardActivityResultHandler)
    }

}



fun Fragment.openScanPaymentCard() {
    val bouncerKey = LocalStoreUtils.getAppSharedPref(
        LocalStoreUtils.KEY_BOUNCER_KEY
    ) as String
    shouldShowLogo()
    CardScanActivity.start(
        this,
        bouncerKey,
        enableEnterCardManually = true,
        enableExpiryExtraction = false,
        enableNameExtraction = false
    )

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

        (requireActivity() as MainActivity).firebaseAnalytics.logEvent(name, bundle)
    }
}

private fun shouldShowLogo(shouldShow:Boolean = false){
    com.getbouncer.scan.framework.Config.displayLogo = shouldShow
}