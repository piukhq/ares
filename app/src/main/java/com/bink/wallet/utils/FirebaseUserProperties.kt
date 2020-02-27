@file:Suppress("DEPRECATION")

package com.bink.wallet.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.util.DisplayMetrics
import com.bink.wallet.R
import com.bink.wallet.utils.enums.ConnectionType
import com.bink.wallet.utils.enums.DeviceZoom
import com.google.firebase.analytics.FirebaseAnalytics


object FirebaseUserProperties {

    const val OS_VERSION = "osVersion"
    const val NETWORK_STRENGTH = "networkStrength"
    const val DEVICE_ZOOM = "deviceZoom"
    const val BINK_VERSION = "binkVersion"

    fun setUserProperty(firebaseAnalytics: FirebaseAnalytics, key: String, value: String) {
        firebaseAnalytics.setUserProperty(key, value)
    }

    fun retrieveZoomStatus(context: Context): String =
        if (context.resources.displayMetrics.densityDpi != DisplayMetrics.DENSITY_DEFAULT) {
            DeviceZoom.ZOOMED.type
        } else {
            DeviceZoom.STANDARD.type
        }

    fun retrieveNetworkStatus(context: Context): String {
        val connMgr =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connMgr.activeNetworkInfo as NetworkInfo
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

    fun retrieveBinkVersion(context: Context): String {
        context.packageManager.getPackageInfo(context.applicationContext.packageName, 0).let {
            val appVersion = it.versionName
            val buildNumber = it.versionCode.toString()
            return context.resources.getString(R.string.bink_version, appVersion, buildNumber)
        }
    }
}