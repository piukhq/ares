package com.bink.wallet.utils

import io.sentry.core.Sentry

object SentryUtils {

    fun logError(sentryError: SentryErrorType, userInfo: String?) {
        Sentry.withScope { scope ->
            userInfo?.let { info -> scope.setExtra("userInfo", info) }
            Sentry.captureException(Exception("${sentryError.localCode} - ${sentryError.issue}"))
            scope.clear()
        }
    }

}

enum class SentryErrorType(val localCode: Int, val issue: String) {
    INVALID_PAYLOAD(3000, "Could not construct payload for tokenisation"),
    TOKEN_REJECTED(3001, "Tokenisation service rejected request"),
    API_REJECTED(3002, "Bink API rejected request")
}

enum class InvalidPayloadType(val error: String) {
    INVALID_HASH("Failed to encrypt hash"),
    INVALID_MONTH("Failed to encrypt expiry month"),
    INVALID_YEAR("Failed to encrypt expiry year"),
    INVALID_FIRST_SIX("Failed to encrypt first six"),
    INVALID_LAST_FOUR("Failed to encrypt last four")
}