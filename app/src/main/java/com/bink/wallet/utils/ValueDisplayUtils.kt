package com.bink.wallet.utils

object ValueDisplayUtils {
    fun displayValue(
        inValue: Float?,
        prefix: String?,
        suffix: String?,
        currency: String? = null,
        type: String? = null
    ): String {
        with (StringBuilder()) {
            if (!prefix.isNullOrEmpty()) {
                append(prefix)
            }
            val value = inValue ?: FLOAT_ZERO
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