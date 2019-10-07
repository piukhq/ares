package com.bink.wallet.model.payment_card

enum class PaymentCardType(val type: Int) {
    NONE(-1),
    AMEX(0),
    VISA(1),
    MASTERCARD(2)
}