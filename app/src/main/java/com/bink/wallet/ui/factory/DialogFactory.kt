package com.bink.wallet.ui.factory

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.bink.wallet.R

class DialogFactory {

    companion object {

        fun showPermissionsSettingsDialog(activity: Activity, negativeAction: () -> Unit = {}) {
            val builder = AlertDialog.Builder(activity)
            builder.setTitle(activity.getString(R.string.add_loyalty_card_permissions_denied_title))
            builder.setMessage(activity.getString(R.string.add_loyalty_card_permissions_denied_message))
            builder.setNegativeButton(activity.getString(R.string.add_loyalty_card_permissions_denied_cta_allow)) { dialogInterface, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                val uri: Uri = Uri.fromParts("package", activity.packageName, null)
                intent.data = uri
                activity.startActivity(intent)
            }

            builder.setPositiveButton(activity.getString(R.string.add_loyalty_card_permissions_denied_cta_enter)) { dialogInterface, _ ->
                negativeAction()
            }

            builder.setNeutralButton(R.string.cancel_text) { dialogInterface, _ ->
                dialogInterface.cancel()
            }

            builder.create().show()
        }

    }

}