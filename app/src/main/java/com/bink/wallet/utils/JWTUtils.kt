package com.bink.wallet.utils

import android.util.Base64.*
import android.util.Log
import java.io.UnsupportedEncodingException

object JWTUtils {

    fun decode(jwt: String): String? {
        try {
            val split = jwt.split("\\.".toRegex()).toTypedArray()
            Log.d("JWT_DECODED", "Header: " + getJson(split[0]))
            Log.d("JWT_DECODED", "Body: " + getJson(split[1]))

            return getJson(split[1])
        } catch (e: UnsupportedEncodingException) {}

        return null
    }

    private fun getJson(strEncoded: String): String {
        val decodedBytes: ByteArray = decode(strEncoded, URL_SAFE)
        return String(decodedBytes)
    }

}