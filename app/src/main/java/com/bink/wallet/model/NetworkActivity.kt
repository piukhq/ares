package com.bink.wallet.model

import com.bink.wallet.data.SharedPreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

data class NetworkActivity(val baseUrl: String, val httpStatusCode: String, val responseBody: String, val endpoint: String, val responseTime: String)

fun NetworkActivity.store() {
    val currentLogs = SharedPreferenceManager.networkExports
    val type: Type = object : TypeToken<ArrayList<NetworkActivity?>?>() {}.type
    val gson = Gson()

    val logsAsArray = if (currentLogs == null) {
        arrayListOf<NetworkActivity>()
    } else {
        gson.fromJson(currentLogs, type)
    }
    logsAsArray.add(0, this)
    if (logsAsArray.size > 20) {
        logsAsArray.removeLast()
    }
    SharedPreferenceManager.networkExports = gson.toJson(logsAsArray)
}