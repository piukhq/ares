package com.bink.wallet.utils

import android.util.Base64
import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.MessageDigest
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher

class SecurityUtils {

    companion object {

        fun getPaymentCardHash(
            pan: String,
            month: String,
            year: String
        ): String {
            if (pan.isEmpty() || month.isEmpty() || year.isEmpty()) {
                return ""
            }

            val hashSecret = LocalStoreUtils.getAppSharedPref(
                LocalStoreUtils.KEY_PAYMENT_HASH_SECRET
            )

            return getSHA512("\\($pan)\\($month)\\($year)\\($hashSecret)")
        }

        private fun getSHA512(input: String): String {
            val md: MessageDigest = MessageDigest.getInstance("SHA-512")
            val messageDigest = md.digest(input.toByteArray())

            // Convert byte array into signum representation
            val no = BigInteger(1, messageDigest)

            // Convert message digest into hex value
            var hashtext: String = no.toString(16)

            // Add preceding 0s to make it 32 bit
            while (hashtext.length < 32) {
                hashtext = "0$hashtext"
            }

            // return the HashText
            return hashtext
        }

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
                return ""
            }

            val androidEncode = Base64.encode(bytes, Base64.NO_WRAP)
            return String(androidEncode, StandardCharsets.UTF_8)
        }

        private fun loadPublicKey(publicKeyString: String): PublicKey? {
            try {
                val byteKey = Base64.decode(publicKeyString.toByteArray(), Base64.DEFAULT)
                val X509publicKey = X509EncodedKeySpec(byteKey)
                val kf = KeyFactory.getInstance("RSA")

                return kf.generatePublic(X509publicKey)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return null
        }
    }


}