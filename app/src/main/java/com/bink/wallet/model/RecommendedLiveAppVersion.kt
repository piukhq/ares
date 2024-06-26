package com.bink.wallet.model

import com.bink.wallet.BuildConfig
import com.bink.wallet.data.SharedPreferenceManager

data class RecommendedLiveAppVersion(val android: String)

fun AppConfiguration.isNewVersionAvailable(): Boolean {
    return try {
        val appVersion = BuildConfig.VERSION_NAME.replace(".", "").toInt()
        val newVersion = this.recommended_live_app_version.android.replace(".", "").toInt()

        (SharedPreferenceManager.skippedAppVersion < newVersion) && (appVersion < newVersion)
    } catch (e: Exception) {
        return false
    }
}

fun AppConfiguration.skipVersion() {
    try {
        val newVersion = this.recommended_live_app_version.android.replace(".", "").toInt()
        SharedPreferenceManager.skippedAppVersion = newVersion
    } catch (e: Exception) {
    }
}