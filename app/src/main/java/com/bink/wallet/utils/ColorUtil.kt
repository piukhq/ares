package com.bink.wallet.utils

import android.graphics.Color
import kotlin.math.max
import kotlin.math.min

class ColorUtil {

    companion object {

        const val LIGHT_THRESHOLD_TEXT = 0.8f
        const val LIGHT_THRESHOLD = 0.5f
        const val COLOR_CHANGE_PERCENTAGE = 30.0f
        const val HEX_FORMAT = "#%02x%02x%02x"
        const val HEX_ALPHA_FORMAT = "#%02x%02x%02x%02x"
        const val ALPHA_PERCENT = 70.0f
        const val MAX_RGB_SCALE = 255

        // The algorithm used here is from http://www.w3.org/WAI/ER/WD-AERT/#color-contrast
        fun isColorLight(color: Int, threshold: Float = LIGHT_THRESHOLD): Boolean {
            val red = Color.red(color)
            val green = Color.green(color)
            val blue = Color.blue(color)

            val maximumBrightness =
                (((MAX_RGB_SCALE * 299.0) + (MAX_RGB_SCALE * 587.0) + (MAX_RGB_SCALE * 114.0)) / 1000.0)
            val brightness = (((red * 299.0) + (green * 587.0) + (blue * 114.0)) / 1000.0)
            val actualBrightness = brightness / maximumBrightness

            return actualBrightness > threshold
        }

        fun darkenColor(color: Int, percentage: Float = COLOR_CHANGE_PERCENTAGE): String {
            return adjustColor(color, percentage, true)
        }

        fun lightenColor(color: Int, percentage: Float = COLOR_CHANGE_PERCENTAGE): String {
            return adjustColor(color, percentage, false)
        }

        private fun adjustColor(color: Int, percentage: Float, shouldDarkenColour: Boolean): String {
            val red = Color.red(color)
            val green = Color.green(color)
            val blue = Color.blue(color)

            val percentageRed: Float
            val percentageGreen: Float
            val percentageBlue: Float

            // If the colour is black, we need to take a % of the total RGB range. This
            // allows us to calculate how much to lighten the colour by.
            if (red == 0 && green == 0 && blue == 0) {
                percentageRed = MAX_RGB_SCALE * percentage / 100
                percentageGreen = MAX_RGB_SCALE * percentage / 100
                percentageBlue = MAX_RGB_SCALE * percentage / 100
            } else {
                percentageRed = red * percentage / 100
                percentageGreen = green * percentage / 100
                percentageBlue = blue * percentage / 100
            }

            // Lighten or darken the colour
            val updatedRed =
                if (shouldDarkenColour) red.minus(percentageRed) else red.plus(
                    percentageRed
                )
            val updatedGreen =
                if (shouldDarkenColour) green.minus(percentageGreen) else green.plus(
                    percentageGreen
                )
            val updatedBlue =
                if (shouldDarkenColour) blue.minus(percentageBlue) else blue.plus(
                    percentageBlue
                )

            // Workout the new colour, we want to avoid negatives and colours greater than
            // the RGB scale
            val newRed = min(max(0f, updatedRed), MAX_RGB_SCALE.toFloat())
            val newGreen = min(max(0f, updatedGreen), MAX_RGB_SCALE.toFloat())
            val newBlue = min(max(0f, updatedBlue), MAX_RGB_SCALE.toFloat())

            val newHex: String

            if (shouldDarkenColour) {
                newHex =
                    String.format(HEX_FORMAT, newRed.toInt(), newGreen.toInt(), newBlue.toInt())
            } else {
                // If the colour is deemed dark therefore meaning we need to lighten the secondary
                // color, we'll also add an opacity to the hex. 
                val alpha = (MAX_RGB_SCALE * ALPHA_PERCENT / 100).toInt()
                newHex = String.format(
                    HEX_ALPHA_FORMAT,
                    alpha,
                    newRed.toInt(),
                    newGreen.toInt(),
                    newBlue.toInt()
                )
            }

            return newHex
        }
    }

}