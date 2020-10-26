package com.bink.wallet.utils

import androidx.databinding.BindingAdapter
import com.bink.wallet.stampsprogressindicator.StampsProgressIndicator
import com.bink.wallet.utils.enums.VoucherStates

@BindingAdapter("maxProgress", "currentProgress", "status")
fun StampsProgressIndicator.setStatus(
    maxProgress: Int,
    currentProgress: Int,
    voucherStatus: String
) {
    if (voucherStatus == VoucherStates.EXPIRED.state ||
        voucherStatus == VoucherStates.REDEEMED.state ||
        voucherStatus == VoucherStates.ISSUED.state
    ) {
        setupStamps(maxProgress, maxProgress, voucherStatus)
    } else {
        setupStamps(maxProgress, currentProgress, voucherStatus)

    }
}