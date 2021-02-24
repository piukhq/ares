package com.bink.wallet.model

data class PointScrapeResponse(var success: Boolean = false, val error_message: String?, val points: String?) {
    fun isDone(): Boolean {
        if (success) {
            if (points != null) {
                return true
            }
        }

        return false
    }
}