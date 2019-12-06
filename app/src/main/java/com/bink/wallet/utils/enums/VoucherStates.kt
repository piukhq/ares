package com.bink.wallet.utils.enums

enum class VoucherStates(val state: String) {
    IN_PROGRESS("inprogress"),
    ISSUED("issued"),
    EXPIRED("expired"),
    REDEEMED("redeemed")
}