package com.bink.wallet.utils.enums

enum class HandledException(val code: Int) {
    BAD_REQUEST(400),
    NOT_FOUND(404),
    INTERNAL_SERVER_ERROR(500),
    SERVICE_UNAVAILABLE(503),
    UNHANDLED(-1)
}
