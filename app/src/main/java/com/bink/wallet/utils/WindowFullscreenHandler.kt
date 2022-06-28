package com.bink.wallet.utils

import android.app.Activity
import android.os.Build
import android.view.WindowInsets
import android.view.WindowManager

class WindowFullscreenHandler(val activity: Activity) {

    fun toNormalScreen() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity.window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            activity.window.clearFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
    }
}