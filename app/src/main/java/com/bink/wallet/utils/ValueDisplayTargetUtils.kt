package com.bink.wallet.utils

import android.content.Context
import com.bink.wallet.R
import com.bink.wallet.model.response.membership_card.Burn
import com.bink.wallet.model.response.membership_card.Earn
import com.bink.wallet.model.response.membership_card.Voucher
import com.bink.wallet.utils.enums.VoucherStates

fun Context.displayVoucherEarnAndTarget(voucher: Voucher): String {
    voucher.earn?.target_value?.let { target_value ->
        if (target_value != FLOAT_ZERO) {
            val earn = voucher.earn
            val burn = voucher.burn
            earn.value?.let { earn_value ->
                return when (voucher.state) {
                    VoucherStates.IN_PROGRESS.state -> setInProgressVoucherValues(this, earn)
                    else ->
                        getString(
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
    }
    return EMPTY_STRING
}

fun setInProgressVoucherValues(
    context: Context,
    earn: Earn
): String {

    return context.getString(
        R.string.loyalty_wallet_plr_value,
        ValueDisplayUtils.displayValue(
            earn.value,
            earn.prefix,
            null,
            earn.currency
        ),
        ValueDisplayUtils.displayValue(
            earn.target_value,
            earn.prefix,
            earn.suffix,
            earn.currency
        )
    )

}
