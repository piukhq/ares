package com.bink.wallet.utils

import android.content.Context
import com.bink.wallet.R
import com.bink.wallet.model.response.membership_card.Voucher

fun Context.displayVoucherEarnAndTarget(voucher: Voucher): String {
    voucher.earn?.target_value?.let { target_value ->
        if (target_value != FLOAT_ZERO) {
            val earn = voucher.earn
            val burn = voucher.burn
            earn.value?.let { earn_value ->
                return getString(
                    R.string.loyalty_wallet_plr_value,
                    ValueDisplayUtils.displayValue(
                        earn_value,
                        earn.prefix,
                        earn.suffix,
                        earn.currency
                    ),
                    ValueDisplayUtils.displayValue(
                        target_value,
                        burn?.prefix,
                        burn?.suffix,
                        burn?.currency
                    )
                )
            }
        }
    }
    return EMPTY_STRING
}
