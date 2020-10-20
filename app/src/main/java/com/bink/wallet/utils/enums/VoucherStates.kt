package com.bink.wallet.utils.enums

import com.bink.wallet.R

enum class VoucherStates(
    val state: String,
    val title: Int?,
    val text: Int?,
    val code: Boolean,
    val date: Int?
) {
    IN_PROGRESS(
        "inprogress",
        null,
        R.string.voucher_detail_text_in_progress,
        false,
        null
    ),
    ISSUED(
        "issued",
        R.string.voucher_detail_title_issued,
        null,
        true,
        R.string.voucher_detail_date_issued
    ),
    EXPIRED(
        "expired",
        R.string.voucher_detail_title_expired,
        null,
        false,
        R.string.voucher_detail_date_expired
    ),
    REDEEMED(
        "redeemed",
        R.string.voucher_detail_title_redeemed,
        null,
        true,
        R.string.voucher_detail_date_redeemed
    ),
    CANCELLED(
        "cancelled",
        R.string.voucher_stamp_cancelled_title,
        null,
        false,
        null
    ),
    NONE(
        "none",
        null,
        null,
        false,
        null
    )
}