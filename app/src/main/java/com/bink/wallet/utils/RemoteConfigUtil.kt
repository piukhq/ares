package com.bink.wallet.utils

import com.bink.wallet.model.ConfigFile
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class RemoteConfigUtil {
    private fun getAppConfig(): ConfigFile? {
        return try {
            Gson().fromJson(
                FirebaseRemoteConfig.getInstance().getString(REMOTE_CONFIG_APP_CONFIGURATION),
                object : TypeToken<ConfigFile>() {}.type
            )
        } catch (e: Exception) {
            null
        }

    }

    val dynamicActionList = getAppConfig()?.dynamic_actions

    val appConfig = getAppConfig()?.app_config

    val localPointsCollection = getAppConfig()?.local_points_collection

    val beta = getAppConfig()?.beta
}