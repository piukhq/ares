package com.bink.wallet.utils

import android.content.Context
import android.net.ConnectivityManager

class NetworkUtils {

    companion object {

        @Suppress("DEPRECATION")
        fun isConnected(context: Context): Boolean {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = connectivityManager.activeNetworkInfo
            val isNetworkAvailable = networkInfo != null && networkInfo.isConnected
            return isNetworkAvailable
        }
    }

}