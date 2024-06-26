package com.bink.wallet.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.bink.wallet.data.SharedPreferenceManager

object LocalStoreUtils {

    private const val PREF_FILE_NAME = "com.bink.wallet"
    const val KEY_EMAIL = "encrypted_email"
    const val KEY_FIRST_NAME = "encrypted_first_name"
    const val KEY_SECOND_NAME = "encrypted_second_name"
    const val KEY_UID = "encrypted_uid"
    const val KEY_TOKEN = "encrypted_token"
    const val KEY_SPREEDLY = "encrypted_spreedly_token"
    const val KEY_PAYMENT_HASH_SECRET = "payment_hash_secret"
    const val KEY_ENCRYPT_PAYMENT_PUBLIC_KEY = "payment_encryption_public_key"
    const val KEY_BOUNCER_KEY = "bouncer_key"

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

    fun removeKey(secretKey: String) {
        encryptedSharedPreferences.edit().let {
            it.remove(secretKey)
            it.apply()
        }
    }

    fun createEncryptedPrefs(context: Context) {
        val masterKeyBuilder = MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        val masterKey = masterKeyBuilder.build()
        encryptedSharedPreferences = EncryptedSharedPreferences.create(
            context,
            PREF_FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun isLoggedIn(key: String): Boolean {
        return encryptedSharedPreferences.contains(key)
    }

    fun clearPreferences() {
        SharedPreferenceManager.clear()
        encryptedSharedPreferences.edit().let {
            it.clear()
            it.apply()
            it.commit()
        }
    }
}