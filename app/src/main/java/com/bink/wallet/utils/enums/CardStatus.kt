package com.bink.wallet.utils.enums

enum class CardStatus(val status: String) {
    AUTHORISED("authorised"),
    PENDING("pending"),
    UNAUTHORISED("unauthorised"),
    FAILED("failed")
}
