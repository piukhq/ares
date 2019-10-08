package com.bink.wallet.model.payment_card

enum class PaymentCardType(val type: Int, val len: Int, val prefix: String, val format: String, val stars: String) {
    NONE(-1, 16, "", "0000000000000000", ""),
    AMEX(0, 15, "3|34|37", "0000 000000 00000", "**** ****** *0000"),
    VISA(1, 16, "4", "0000 0000 0000 0000", "**** **** **** 0000"),
    MASTERCARD(2, 16, "5", "0000 0000 0000 0000","**** **** **** 0000")
}