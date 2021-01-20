package com.bink.wallet.utils

import io.sentry.core.Sentry

object SentryUtils {

    fun logError(sentryError: SentryErrorType, extraInfo: String?) {
        Sentry.captureException(SentryError(sentryError.errorCode, sentryError.issue))
    }

}

enum class SentryErrorType(val errorCode: Int, val issue: String) {
    INVALID_PAYLOAD(3000, "Could not construct payload for tokenisation"),
    TOKEN_REJECTED(3001, "Tokenisation service rejected request"),
    API_REJECTED(3001, "Bink API rejected request")
}

enum class InvalidPayloadType(val error: String) {
    INVALID_HASH("Failed to encrypt hash"),
    INVALID_MONTH("Failed to encrypt expiry month"),
    INVALID_YEAR("Failed to encrypt expiry year"),
    INVALID_FIRST_SIX("Failed to encrypt first six"),
    INVALID_LAST_FOUR("Failed to encrypt last four")
}

class SentryError(val code: Int, override val message: String) : RuntimeException()