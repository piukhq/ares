package com.bink.wallet.model

import com.bink.wallet.BuildConfig

data class AppConfiguration(val recommended_live_app_version: RecommendedLiveAppVersion)

data class RecommendedLiveAppVersion(val android_version: String)

fun AppConfiguration.isNewVersionAvailable(): Boolean {
    return try {
        val appVersion = BuildConfig.VERSION_NAME.replace(".", "").toInt()
        val newVersion = this.recommended_live_app_version.android_version.replace(".", "").toInt()

        appVersion < newVersion
    } catch (e: Exception) {
        return false
    }
}