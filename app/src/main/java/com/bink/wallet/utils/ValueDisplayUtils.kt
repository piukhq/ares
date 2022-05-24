package com.bink.wallet.utils

import com.bink.wallet.model.response.membership_card.Earn
import com.bink.wallet.utils.bindings.ACCUMULATOR
import com.bink.wallet.utils.bindings.STAMP
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
        with(display) {
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

                append(SPACE)
            }
            if (!suffix.isNullOrEmpty()) {
                append(suffix)
                append(SPACE)
            }
            if (!type.isNullOrEmpty()) {
                append(type)
            }
            return toString()
        }
    }

    fun displayFormattedHeadline(
        voucher: Earn?

    ): String {
        val prefix = voucher?.prefix
        val targetValue: Float? = voucher?.target_value
        val value: Float? = voucher?.value
        val suffix = voucher?.suffix
        var difference: Float = 0.0F

        val headline = StringBuilder()

        with(headline) {
            if (!prefix.isNullOrEmpty()) {
                headline.append(prefix)

            }

            targetValue?.let { targetValue ->
                value?.let { value ->
                    difference = (targetValue - value)
                    if (voucher.type != ACCUMULATOR && !prefix.isNullOrEmpty()) {
                        append(SPACE)
                    }
                    append(
                        if (difference != difference.toInt()
                                .toFloat()
                        ) TWO_DECIMALS_FLOAT_FORMAT.format(
                            Locale.ENGLISH,
                            difference
                        ) else NO_DECIMALS_FORMAT.format(difference)

                    )

                }

            }
            suffix?.let {
                append(SPACE)
                if (voucher.type == STAMP) {
                    val suffixValue = if (difference.toInt() > 1) it else "stamp"
                    append(suffixValue)
                } else {
                    append(it)
                }
            }
            return toString()
        }

    }
}
