package com.bink.wallet.utils

import android.util.Base64.*
import android.util.Log
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException

object JWTUtils {

    fun decode(jwt: String): String? {
        try {
            val split = jwt.split("\\.".toRegex()).toTypedArray()
            Log.d("JWT_DECODED", "Header: " + getJson(split[0]))
            Log.d("JWT_DECODED", "Body: " + getJson(split[1]))

            return getJson(split[1])
        } catch (e: UnsupportedEncodingException) {
        }

        return null
    }

    fun getEmailFromJson(token: String): String? {
        return try {
            JSONObject(token).getString("email")
        } catch (e: JSONException) {
            try {
                JSONObject(token).getString("user_id")
            } catch (e: JSONException) {
                null
            }
        }

    }

    private fun getJson(strEncoded: String): String {
        val decodedBytes: ByteArray = decode(strEncoded, URL_SAFE)
        return String(decodedBytes)
    }

}