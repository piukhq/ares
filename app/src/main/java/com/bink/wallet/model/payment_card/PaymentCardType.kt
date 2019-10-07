package com.bink.wallet.model.payment_card

enum class PaymentCardType(val type: Int, val len: Int, val format: String) {
    NONE(-1, -1, ""),
    AMEX(0, 15, "**** ***** **0000"),
    VISA(1, 16, "**** **** **** 0000"),
    MASTERCARD(2, 16, "**** **** **** 0000")
}