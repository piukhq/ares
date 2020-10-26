package com.bink.wallet.utils

import java.util.*

object ValueDisplayUtils {
    fun displayValue(
        value: Float?,
        prefix: String?,
        suffix: String?,
        currency: String?,
        type: String? = null,
        forceTwoDecimals: Boolean = false
    ): String {
        val display = StringBuilder()
        with (display) {
            if (!prefix.isNullOrEmpty()) {
                append(prefix)
            }
            value?.let {
                append(
                    if (value != it.toInt().toFloat() ||
                        forceTwoDecimals
                    ) {
                        TWO_DECIMALS_FLOAT_FORMAT.format(Locale.ENGLISH, it)
                    } else {
                        NO_DECIMALS_FORMAT.format(it)
                    }
                )
            }
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
