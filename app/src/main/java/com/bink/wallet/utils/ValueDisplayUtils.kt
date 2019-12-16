package com.bink.wallet.utils

object ValueDisplayUtils {
    fun displayValue(
        inValue: Float?,
        prefix: String?,
        suffix: String?,
        currency: String? = null,
        type: String? = null
    ): String {
        val display = StringBuilder()
        if (!prefix.isNullOrEmpty()) {
            display.append(prefix)
        }
        val value = inValue ?: FLOAT_ZERO
        display.append(
            if (value != value.toInt().toFloat()) {
                "%.2f".format(value)
            } else {
                "%.0f".format(value)
            }
        )
        if (!suffix.isNullOrEmpty()) {
            display.append(SPACE)
            display.append(suffix)
        }
        if (!type.isNullOrEmpty()) {
            display.append(SPACE)
            display.append(type)
        }
        return display.toString()
    }
}