package com.bink.wallet.utils

import android.util.Base64
import android.util.Log
import com.bink.sdk.BinkCore
import com.bink.sdk.util.BinkSecurityUtil
import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

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

//            val data = Base64.decode(hashSecret, Base64.DEFAULT)
//            val text = String(data, StandardCharsets.UTF_8)

//            Log.e("ConnorDebug", "hashSecret: " + BinkCore().sessionConfig.encryptSomething("Uf5AIu6ehfHM2By6GAsIDsclEYFlWQNt"))
//            Log.e("ConnorDebug", "decoded: " + text)
            return getSHA512("\\($pan)\\($month)\\($year)\\(Uf5AIu6ehfHM2By6GAsIDsclEYFlWQNt)")
        }

        fun getSHA512(input: String): String {
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
    }


}