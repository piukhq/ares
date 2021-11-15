package com.bink.wallet.utils

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.bink.wallet.data.SharedPreferenceManager
import com.bink.wallet.databinding.DialogCameraAccessBinding

class CameraAccessDialog(private val activity: Activity, private val activityResult: ActivityResultLauncher<String>?, private val negativeAction: () -> Unit = {}) : Dialog(activity) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DialogCameraAccessBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)

        binding.allowAccess.setOnClickListener {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val uri: Uri = Uri.fromParts("package", activity.packageName, null)
            intent.data = uri
            activity.startActivity(intent)
            this.cancel()
        }

        binding.enterManually.setOnClickListener {
            negativeAction()
            this.cancel()
        }

        binding.fromPhotoLibrary.setOnClickListener {
            if (activityResult != null) {
                activityResult.launch("image/*")
                this.cancel()
            }
        }

        binding.cancel.setOnClickListener {
            this.cancel()
        }

        if (SharedPreferenceManager.didAttemptToAddPaymentCard) binding.fromPhotoLibrary.visibility = View.GONE else binding.fromPhotoLibrary.visibility = View.VISIBLE
    }
}