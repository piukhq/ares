package com.bink.wallet.model.payment_card

enum class PaymentCardType(val type: Int, val len: Int, val prefix: String, val format: String) {
    NONE(-1, -1, "", ""),
    AMEX(0, 15, "3|34|35", "**** ***** **0000"),
    VISA(1, 16, "4", "**** **** **** 0000"),
    MASTERCARD(2, 16, "5","**** **** **** 0000")
}