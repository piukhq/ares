package com.bink.wallet.utils.enums

enum class LoyaltyCardLinkStatus(val status: String) {
    NONE(""),
    LINKED("linked"),
    LINK_NOW("link_now"),
    RETRY("retry"),
    PENDING("pending")
}