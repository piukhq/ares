package com.bink.wallet.utils

import android.content.Context
import android.util.Base64
import com.bink.wallet.scenes.login.LoginRepository
import org.json.JSONObject
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class JwtCreator(private val repo: LoginRepository) {

    fun createJwt(context: Context): String {
        val header = JSONObject()
        header.put(JWT_HEADER_NAME_ALGORITHM, JWT_HEADER_VALUE_ALGORITHM)
        header.put(JWT_HEADER_NAME_TYPE, JWT_HEADER_VALUE_TYPE)

        val payload = JSONObject()
        payload.put(JWT_PAYLOAD_TYPE_ORGANISATION, JWT_PAYLOAD_VALUE_ORGANISATION)
        payload.put(JWT_PAYLOAD_TYPE_BUNDLE, JWT_PAYLOAD_VALUE_BUNDLE)
        payload.put(JWT_PAYLOAD_TYPE_USER, repo.loginEmail)
        payload.put(JWT_PAYLOAD_TYPE_PROPERTY, JWT_PAYLOAD_VALUE_PROPERTY)
        payload.put(JWT_PAYLOAD_TYPE_TIME, System.currentTimeMillis() / 1000)

        val token = String.format(
            "%s.%s",
            Base64.encodeToString(
                header.toString().toByteArray(),
                Base64.URL_SAFE
            ),
            Base64.encodeToString(
                payload.toString().toByteArray(),
                Base64.URL_SAFE
            )
        )
            .headerTidy()

        val hmac = Mac.getInstance(HMAC_TYPE)

        val secretKey = SecretKeySpec(
            LocalStoreUtils.getAppSharedPref(
                LocalStoreUtils.KEY_SECRET,
                context
            )?.toByteArray(),
            HMAC_TYPE
        )

        hmac.init(secretKey)

        val signature =
            Base64.encodeToString(
                hmac.doFinal(token.toByteArray()),
                Base64.URL_SAFE
            )
                .headerTidy()

        return "Bearer $token.$signature".headerTidy()
    }
}