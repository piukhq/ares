package com.bink.wallet.utils

import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher

class SecurityUtils {

    companion object {

        fun encryptMessage(messageToEncrypt: String, publicKeyString: String): String {
            val cipher: Cipher
            val publicKey = loadPublicKey(publicKeyString)
            var bytes = ByteArray(0)
            try {
                cipher = Cipher.getInstance("RSA/ECB/OAEPPadding")
                cipher.init(Cipher.ENCRYPT_MODE, publicKey)
                bytes = cipher.doFinal(messageToEncrypt.toByteArray())
            } catch (e: Exception) {
                e.printStackTrace()
                return EMPTY_STRING
            }

            val androidEncode = Base64.encode(bytes, Base64.NO_WRAP)
            return String(androidEncode, StandardCharsets.UTF_8)
        }

        private fun loadPublicKey(publicKeyString: String): PublicKey? {
            try {
                val byteKey = Base64.decode(publicKeyString.toByteArray(), Base64.DEFAULT)
                val X509publicKey = X509EncodedKeySpec(byteKey)
                val keyFactory = KeyFactory.getInstance("RSA")

                return keyFactory.generatePublic(X509publicKey)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return null
        }
    }
}