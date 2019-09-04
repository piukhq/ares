package com.bink.wallet.utils

import android.content.Context
import android.content.SharedPreferences

object LocalStoreUtils {

    private const val PREF_FILE_NAME = "com.bink.wallet"
    const val KEY_SECRET = "api_secret"
    const val KEY_JWT = "kwt_token"

    fun setAppSharedPref(secretKey: String, secret: String, context: Context) {
        try {
            val editor = getSharedEditor(context)
            editor.putString(secretKey, secret)
            editor.commit()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getAppSharedPref(secretKey: String, context: Context): String? {
        try {
            val pref = getSharedPreference(context)
            return pref.getString(secretKey, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun getSharedEditor(context: Context?): SharedPreferences.Editor {
        if (context == null) {
            throw Exception("Context null Exception")
        }
        return getSharedPreference(context).edit()
    }

    private fun getSharedPreference(context: Context?): SharedPreferences {
        if (context == null) {
            throw Exception("Context null Exception")
        }
        return context.getSharedPreferences(PREF_FILE_NAME, 0)
    }

}