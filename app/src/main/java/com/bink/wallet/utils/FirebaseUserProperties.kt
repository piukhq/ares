@file:Suppress("DEPRECATION")

package com.bink.wallet.utils

import android.content.Context
import android.content.res.Configuration
import android.net.ConnectivityManager
import com.bink.wallet.BuildConfig
import com.bink.wallet.R
import com.bink.wallet.utils.enums.BuildTypes
import com.bink.wallet.utils.enums.ConnectionType
import com.bink.wallet.utils.enums.DeviceZoom
import com.google.firebase.analytics.FirebaseAnalytics
import java.util.Locale

object FirebaseUserProperties {
    const val OS_VERSION = "osVersion"
    const val NETWORK_STRENGTH = "networkStrength"
    const val DEVICE_ZOOM = "deviceZoom"
    const val BINK_VERSION = "binkVersion"

    fun setUserProperty(firebaseAnalytics: FirebaseAnalytics, key: String, value: String) {
        if (BuildConfig.BUILD_TYPE.toLowerCase(Locale.ENGLISH) == BuildTypes.RELEASE.type) {
            firebaseAnalytics.setUserProperty(key, value)
        }
    }

    fun retrieveZoomStatus(context: Context): String {
        val screenSize: Int = context.resources.configuration.screenLayout and
                Configuration.SCREENLAYOUT_SIZE_MASK
        return when (screenSize) {
            Configuration.SCREENLAYOUT_SIZE_LARGE,
            Configuration.SCREENLAYOUT_SIZE_XLARGE -> {
                DeviceZoom.ZOOMED.type
            }
            else -> DeviceZoom.STANDARD.type
        }
    }


    fun retrieveNetworkStatus(context: Context): String {
        val connMgr =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connMgr.activeNetworkInfo
        networkInfo?.let {
            return when (networkInfo.type) {
                ConnectivityManager.TYPE_WIFI -> {
                    ConnectionType.WI_FI.type
                }
                ConnectivityManager.TYPE_MOBILE -> {
                    ConnectionType.CELLULAR.type
                }
                else -> {
                    ConnectionType.UNKNOWN.type
                }
            }
        }
        return ConnectionType.UNKNOWN.type
    }

    fun retrieveBinkVersion(context: Context): String {
        context.packageManager.getPackageInfo(context.applicationContext.packageName, 0).let {
            val appVersion = it.versionName
            val buildNumber = it.versionCode.toString()
            return context.resources.getString(R.string.bink_version, appVersion, buildNumber)
        }
    }
}