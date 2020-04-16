package com.bink.wallet.utils.enums

enum class FieldType(val type: Int) {
    TEXT(0),
    SENSITIVE(1),
    SPINNER(2),
    BOOLEAN_OPTIONAL(3),
    BOOLEAN_REQUIRED(4),
    DISPLAY(5),
    HEADER(6)
}