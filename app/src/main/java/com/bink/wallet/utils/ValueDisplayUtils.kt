package com.bink.wallet.utils

import android.content.Context
import com.bink.wallet.R
import com.bink.wallet.model.response.membership_card.Voucher

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
                        forceTwoDecimals) {
                        "%.2f".format(it)
                    } else {
                        "%.0f".format(it)
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
