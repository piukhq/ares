package com.bink.wallet.ui.factory

import android.app.Activity
import android.app.AlertDialog
import androidx.activity.result.ActivityResultLauncher
import com.bink.wallet.R
import com.bink.wallet.utils.CameraAccessDialog

class DialogFactory {

    companion object {

        fun showPermissionsSettingsDialog(activity: Activity, activityResult: ActivityResultLauncher<String>?, negativeAction: () -> Unit = {}) {
            val dialog = CameraAccessDialog(activity, activityResult, negativeAction)
            dialog.show()
        }

        fun showTryAgainGenericError(
            activity: Activity
        ) {
            val builder = AlertDialog.Builder(activity)
            builder.setTitle(activity.getString(R.string.payment_card_scanning_scan_failed_title))
            builder.setMessage(activity.getString(R.string.payment_card_scanning_scan_failed))
            builder.setPositiveButton(activity.getString(android.R.string.ok)) { dialogInterface, _ ->
                dialogInterface.cancel()
            }

            builder.create().show()
        }

    }

}