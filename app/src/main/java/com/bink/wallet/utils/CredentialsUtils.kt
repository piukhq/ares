package com.bink.wallet.utils

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.MasterKeys
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec


/**
 */
object CredentialsUtils {

    private val keyStore: KeyStore = createAndroidKeyStore()
    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    private const val provider = "AndroidKeyStore"
    private const val ivKey = "iv"
    private const val encryptedKey = "encrypted"
    private var transformation = ""

    fun createNewKey(): SecretKey {
        val algorithm = KeyProperties.KEY_ALGORITHM_AES
        val mode = KeyProperties.BLOCK_MODE_GCM
        val padding = KeyProperties.ENCRYPTION_PADDING_NONE


        val keyGenerator = KeyGenerator.getInstance(algorithm, provider)
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            masterKeyAlias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(mode)
            .setEncryptionPaddings(padding)
            .setRandomizedEncryptionRequired(true)
            .build()
        keyGenerator.init(keyGenParameterSpec)
        transformation = "$algorithm/$mode/$padding"

        return keyGenerator.generateKey()
    }

    fun encrypt(decryptedString: String): String {
        val map = HashMap<String, ByteArray>()
        try {
            val decryptedBytes = decryptedString.toByteArray()

            val secretKeyEntry = keyStore.getEntry(masterKeyAlias, null) as KeyStore.SecretKeyEntry
            val secretKey = secretKeyEntry.secretKey

            val cipher = Cipher.getInstance(transformation)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val ivBytes = cipher.iv
            val encryptedBytes = cipher.doFinal(decryptedBytes)
            map[ivKey] = ivBytes
            map[encryptedKey] = encryptedBytes
        } catch (e: Throwable) {
            e.printStackTrace()
        }

        return hashMapToString(map)
    }

    fun decrypt(encryptedString: String): String {
        val map = stringToHashMap(encryptedString)

        var decryptedResult = ""
        try {
            val secretKeyEntry = keyStore.getEntry(masterKeyAlias, null) as KeyStore.SecretKeyEntry
            val secretKey = secretKeyEntry.secretKey

            val encryptedBytes = map[encryptedKey]
            val ivBytes = map[ivKey]

            val cipher = Cipher.getInstance(transformation)
            val spec = GCMParameterSpec(128, ivBytes)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
            decryptedResult = String(cipher.doFinal(encryptedBytes))
        } catch (e: Throwable) {
            e.printStackTrace()
        }

        return decryptedResult
    }

    private fun hashMapToString(map: HashMap<String, ByteArray>): String {
        return Gson().toJson(map)
    }

    private fun stringToHashMap(string: String): HashMap<String, ByteArray> {
        val type = object : TypeToken<HashMap<String, ByteArray>>() {}.type
        return Gson().fromJson(string, type)
    }

    private fun createAndroidKeyStore(): KeyStore {
        val keyStore = KeyStore.getInstance(provider)
        keyStore.load(null)
        return keyStore
    }
}