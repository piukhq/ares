package com.bink.wallet.utils

import android.graphics.Color
import kotlin.math.max
import kotlin.math.min

class ColorUtil {

    companion object {

        const val BLACK = "#000000"
        const val SECONDARY_COLOR_BLACK = "#a9a9a9"
        const val LIGHT_THRESHOLD = 0.5f
        const val COLOR_CHANGE_PERCENTAGE = 30.0f
        const val HEX_FORMAT = "#%02x%02x%02x"

        // The algorithm used here is from http://www.w3.org/WAI/ER/WD-AERT/#color-contrast
        fun isColorLight(color: Int, threshold: Float = LIGHT_THRESHOLD): Boolean {
            val red = Color.red(color)
            val green = Color.green(color)
            val blue = Color.blue(color)

            val brightness = ((red * 299) + (green * 587) + (blue * 144) / 1000)
            return brightness > threshold
        }

        fun darkenColor(color: Int, percentage: Float = COLOR_CHANGE_PERCENTAGE): String {
            return adjustColor(color, percentage, true)
        }

        fun lightenColor(color: Int, percentage: Float = COLOR_CHANGE_PERCENTAGE): String {
            return adjustColor(color, percentage, false)
        }

        private fun adjustColor(color: Int, percentage: Float, isDark: Boolean): String {
            val percentageOfRed = Color.red(color) * percentage / 100
            val percentageOfGreen = Color.green(color) * percentage / 100
            val percentageOfBlue = Color.blue(color) * percentage / 100

            val updatedRed = if (isDark) Color.red(color).minus(percentageOfRed) else Color.red(color).plus(percentageOfRed)
            val updatedGreen = if (isDark) Color.green(color).minus(percentageOfGreen) else Color.green(color).plus(percentageOfGreen)
            val updatedBlue = if (isDark) Color.blue(color).minus(percentageOfBlue) else Color.blue(color).plus(percentageOfBlue)

            val newRed = min(max(0f, updatedRed), 255f)
            val newGreen = min(max(0f, updatedGreen), 255f)
            val newBlue = min(max(0f, updatedBlue), 255f)

            val newHex = String.format(HEX_FORMAT, newRed.toInt(), newGreen.toInt(), newBlue.toInt())

            if (newHex == BLACK) {
                return SECONDARY_COLOR_BLACK
            }

            return newHex
        }
    }

}