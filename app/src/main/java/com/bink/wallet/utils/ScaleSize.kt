package com.bink.wallet.utils

import android.util.DisplayMetrics
import android.view.WindowManager

object ScaleSize {
    private const val DEFAULT_MARGINS = 32

    fun ScaleSize(windowManager: WindowManager, scale: Float): Int {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels
        val density = displayMetrics.density

        val dpiWidth = (width / density) - DEFAULT_MARGINS
        val scaledSize: Float = dpiWidth / 3

        return (scaledSize * scale * density).toInt()
    }
}