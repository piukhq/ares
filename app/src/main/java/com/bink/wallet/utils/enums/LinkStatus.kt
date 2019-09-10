package com.bink.wallet.utils.enums

enum class LinkStatus(val status: Double) {
    STATUS_LINKED_TO_SOME_OR_ALL(2.1),
    STATUS_LINKABLE_NO_PAYMENT_CARDS(2.2),
    STATUS_LINKABLE_NO_PAYMENT_CARDS_LINKED(2.3),
    STATUS_LINKABLE_GENERIC_ERROR(2.4),
    STATUS_LINKABLE_REQUIRES_AUTH(2.5),
    STATUS_LINKABLE_REQUIRES_AUTH_PENDING(2.6),
    STATUS_LINKABLE_REQUIRES_AUTH_PENDING_FAILED(2.7),
    STATUS_UNLINKABLE(2.8),
}