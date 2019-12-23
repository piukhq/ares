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
        with (display) {
            if (!prefix.isNullOrEmpty()) {
                append(prefix)
            }
            append(
                if (value != value.toInt().toFloat()) {
                    "%.2f".format(value)
                } else {
                    "%.0f".format(value)
                }
            )
            if (!suffix.isNullOrEmpty()) {
                append(SPACE)
                append(suffix)
            }
            if (!type.isNullOrEmpty()) {
                append(SPACE)
                append(type)
            }
            return toString()
        }
    }
}
