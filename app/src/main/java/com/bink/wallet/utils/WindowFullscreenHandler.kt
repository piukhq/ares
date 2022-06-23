package com.bink.wallet.utils

import android.app.Activity
import android.view.WindowManager

class WindowFullscreenHandler(val activity: Activity) {

    fun toNormalScreen() {
        activity.window?.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        activity.window?.addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
    }
}