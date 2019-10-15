package com.bink.wallet.utils

import android.util.Base64
import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import kotlin.random.Random

object StringUtils {
    fun randomString(length: Int): String {
        val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        return (1..length)
            .map { Random.nextInt(0, charPool.size) }
            .map(charPool::get)
            .joinToString("");
    }
}

fun String.md5(): String {
//    val md = MessageDigest.getInstance("MD5")
//    return BigInteger(1, md.digest(toByteArray())).toString(16).padStart(32, '0')
//    return String.format("%032x", BigInteger(1, MessageDigest.getInstance("MD5").digest(toByteArray(Charsets.UTF_8))))
    return getMd5Base64(toByteArray())!!
}

fun getMd5Base64(encTarget: ByteArray): String? {
    val mdEnc: MessageDigest?
    try {
        mdEnc = MessageDigest.getInstance("MD5")
        // Encryption algorithmy
        val md5Base16 = BigInteger(1, mdEnc.digest(encTarget))     // calculate md5 hash
        return Base64.encodeToString(md5Base16.toByteArray(), 16).trim()     // convert from base16 to base64 and remove the new line character
    } catch (e: NoSuchAlgorithmException) {
        e.printStackTrace()
        return e.message
    }
}