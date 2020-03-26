package com.bink.wallet.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.bink.sdk.BinkCore
import com.bink.wallet.data.SharedPreferenceManager

object LocalStoreUtils {

    private const val PREF_FILE_NAME = "com.bink.wallet"
    const val KEY_EMAIL = "encrypted_email"
    const val KEY_TOKEN = "encrypted_token"
    const val KEY_SPREEDLY = "encrypted_spreedly_token"
    const val KEY_PAYMENT_HASH_SECRET = "payment_hash_secret"
    const val KEY_ENCRYPT_PAYMENT_PUBLIC_KEY = "payment_encryption_public_key"

    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    private lateinit var encryptedSharedPreferences: SharedPreferences

    fun setAppSharedPref(secretKey: String, secret: String) {
        try {
            encryptedSharedPreferences.edit().let {
                it.putString(secretKey, secret)
                it.apply()
                it.commit()
            }
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

    fun clearPreferences(context: Context) {
        BinkCore(context).sessionConfig.apiKey = null
        SharedPreferenceManager.clear()
        encryptedSharedPreferences.edit().let {
            it.clear()
            it.apply()
            it.commit()
        }
    }
}