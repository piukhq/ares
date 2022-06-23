package com.bink.wallet.utils

import com.google.gson.Gson
import io.sentry.Sentry
import retrofit2.HttpException

object SentryUtils {

    fun logError(sentryError: SentryErrorType, exception: Exception) {
        var userInfo: String? = null

        when (exception) {
            is HttpException -> {
                val errorBody = Gson().toJson(ErrorBody(exception.getErrorBody()))
                userInfo = "Error code: ${exception.code()} - Error Body: $errorBody"
            }
            else -> userInfo = "Error message: ${exception.message}"
        }

        logError(sentryError, userInfo)
    }

    fun logError(sentryError: SentryErrorType, errorMessage: String?) {
        Sentry.withScope { scope ->
            errorMessage?.let { info -> scope.setExtra("error_message", info) }
            Sentry.captureException(Exception("${sentryError.localCode} - ${sentryError.issue}"))
            scope.clear()
        }
    }

    fun logError(sentryError: SentryErrorType, errorMessage: String?, merchant: String?, isRefresh: Boolean?) {
        Sentry.withScope { scope ->
            isRefresh?.let { scope.setExtra("balance_refresh", (!it).toString()) }
            errorMessage?.let { scope.setExtra("error_message", it) }
            merchant?.let { scope.setExtra("merchant", it) }
            Sentry.captureException(Exception("${sentryError.localCode} - ${sentryError.issue}"))
            scope.clear()
        }
    }

}

enum class SentryErrorType(val localCode: Int, val issue: String) {
    LOYALTY_INVALID_PAYLOAD(2000, "Could not construct payload"),
    LOYALTY_API_REJECTED(2001, "Bink API rejected request"),
    INVALID_PAYLOAD(3000, "Could not construct payload for tokenisation"),
    TOKEN_REJECTED(3001, "Tokenisation service rejected request"),
    API_REJECTED(3002, "Bink API rejected request"),
    LOCAL_POINTS_SCRAPE_CLIENT(4001, "Local points collection failed"),
    LOCAL_POINTS_SCRAPE_SITE(4002, "Local points collection failed"),
    LOCAL_POINTS_SCRAPE_USER(4003, "Local points collection failed")
}

enum class LocalPointScrapingError(val issue: String) {
    SCRIPT_NOT_FOUND("Script file not found"),
    UNHANDLED_IDLING("Unhandled idling"),
    JS_DECODE_FAILED("Failed to decode javascript response"),
    INCORRECT_CRED("Login failed - incorrect credentials"),
    GENERIC_FAILURE("Local points collection uncategorized failure.")
}

enum class InvalidPayloadType(val error: String) {
    INVALID_MONTH("Failed to encrypt expiry month"),
    INVALID_YEAR("Failed to encrypt expiry year"),
    INVALID_FIRST_SIX("Failed to encrypt first six"),
    INVALID_LAST_FOUR("Failed to encrypt last four")
}
