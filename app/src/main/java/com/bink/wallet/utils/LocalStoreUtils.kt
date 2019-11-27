package com.bink.wallet.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

object LocalStoreUtils {

    private const val PREF_FILE_NAME = "com.bink.wallet"
    const val KEY_EMAIL = "encrypted_email"
    const val KEY_TOKEN = "encrypted_token"

    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    private lateinit var encryptedSharedPreferences: SharedPreferences

    fun setAppSharedPref(secretKey: String, secret: String) {
        try {
            val editor = encryptedSharedPreferences.edit()
            editor.putString(secretKey, secret)
            editor.apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getAppSharedPref(secretKey: String): String? {
        try {
            return encryptedSharedPreferences.getString(secretKey, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun createEncryptedPrefs(context: Context) {
        encryptedSharedPreferences = EncryptedSharedPreferences.create(
            PREF_FILE_NAME,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun isLoggedIn(key: String): Boolean {
        return encryptedSharedPreferences.contains(key)
    }

    fun clearPreferences() {
        val editor = encryptedSharedPreferences.edit()
        editor.clear()
        editor.apply()
    }

}