package com.bink.wallet.utils

import android.content.Context
import android.content.SharedPreferences

object LocalStoreUtils {

    private const val PREF_FILE_NAME = "com.bink.wallet"
    private const val KEY_SECRET = "api_secret"

    fun setAppSecret(secret: String, context: Context) {
        try {
            val editor = getSharedEditor(context)
            editor.putString(KEY_SECRET, secret)
            editor.commit()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getAppSecret(context: Context): String? {
        try {
            val pref = getSharedPreference(context)
            return pref.getString(KEY_SECRET, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    @Throws(Exception::class)
    private fun getSharedEditor(context: Context?): SharedPreferences.Editor {
        if (context == null) {
            throw Exception("Context null Exception")
        }
        return getSharedPreference(context).edit()
    }

    @Throws(Exception::class)
    private fun getSharedPreference(context: Context?): SharedPreferences {
        if (context == null) {
            throw Exception("Context null Exception")
        }
        return context.getSharedPreferences(PREF_FILE_NAME, 0)
    }

}