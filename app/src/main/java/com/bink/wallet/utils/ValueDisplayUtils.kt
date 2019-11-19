package com.bink.wallet.utils

object ValueDisplayUtils {
    fun displayValue(
        value: Float,
        prefix: String?,
        suffix: String?,
        currency: String?,
        type: String? = null
    ): String {
        val display = StringBuilder()
        if (!prefix.isNullOrEmpty()) {
            display.append(prefix)
        }
        display.append(
            if (value != value.toInt().toFloat()) {
                "%.2f".format(value)
            } else {
                "%.0f".format(value)
            }
        )
        if (prefix.isNullOrEmpty() &&
            !suffix.isNullOrEmpty()) {
            display.append(suffix)
        }
        if (!type.isNullOrEmpty()) {
            display.append(" %s".format(type))
        }
        return display.toString()
    }
}