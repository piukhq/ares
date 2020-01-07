package com.bink.wallet.utils

import com.bink.wallet.utils.enums.VoucherStates

fun String?.getVoucherState(): VoucherStates {
    return VoucherStates.values().firstOrNull { it.state == this } ?: VoucherStates.NONE
}