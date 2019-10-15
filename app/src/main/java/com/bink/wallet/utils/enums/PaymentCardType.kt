package com.bink.wallet.utils.enums

import com.bink.wallet.R

enum class PaymentCardType(
    val type: String, val len: Int, val prefix: String, val format: String,
    val background: Int, val logo: Int, val subLogo: Int) {
    NONE(
        "NONE", 16, "", "0000000000000000",
        R.drawable.ic_card_payment_bkgr_none, R.drawable.blank, R.drawable.blank
    ),
    AMEX(
        "American Express", 15, "3|34|37", "0000 000000 00000",
        R.drawable.ic_card_payment_bkgr_am_ex,
        R.drawable.ic_am_ex, R.drawable.ic_am_ex_sub_logo
    ),
    VISA(
        "Visa", 16, "4", "0000 0000 0000 0000",
        R.drawable.ic_card_payment_bkgr_visa,
        R.drawable.ic_visa, R.drawable.ic_visa_sub_logo
    ),
    MASTERCARD(
        "Mastercard", 16, "5", "0000 0000 0000 0000",
        R.drawable.ic_card_payment_bkgr_master_card,
        R.drawable.ic_master_card, R.drawable.ic_master_card_sub_logo
    )
}
