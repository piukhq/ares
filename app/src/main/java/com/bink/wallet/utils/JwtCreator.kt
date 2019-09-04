package com.bink.wallet.utils

import android.content.Context
import android.util.Base64
import org.json.JSONObject
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object JwtCreator {

    fun createJwt(context: Context): String {
        val header = JSONObject()
        header.put("alg", "HS512")
        header.put("typ", "JWT")

        val payload = JSONObject()
        payload.put("organisation_id", "Loyalty Angels")
        payload.put("bundle_id", "com.bink.bink20dev")
        payload.put("user_id", "Bink20iteration1@testbink.com")
        payload.put("property_id", "not currently used for authentication")
        payload.put("iat", System.currentTimeMillis() / 1000)

        val token = "${Base64.encodeToString(
            header.toString().toByteArray(), Base64.URL_SAFE
        )}.${Base64.encodeToString(payload.toString().toByteArray(), Base64.URL_SAFE)}".replace("=", "")
            .replace("\n", "")

        val hmac = Mac.getInstance("HmacSHA512")

        val secretKey = SecretKeySpec(
            LocalStoreUtils.getAppSharedPref(LocalStoreUtils.KEY_SECRET, context)?.toByteArray(),
            "HmacSHA512"
        )

        hmac.init(secretKey)

        val signature =
            Base64.encodeToString(hmac.doFinal(token.toByteArray()), Base64.URL_SAFE).replace("=", "").replace("\n", "")

        return "Bearer $token.$signature".replace("=", "").replace("\n", "")
    }
}