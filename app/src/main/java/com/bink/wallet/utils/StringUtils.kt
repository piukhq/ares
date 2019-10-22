package com.bink.wallet.utils

import java.math.BigInteger
import java.security.MessageDigest
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

val ENCRYPTION_TYPE_MD5 = "MD5"
val ENCRYPTION_PAD_CHAR = '0'
val ENCRYPTION_SIGN_NUM = 1
val ENCRYPTION_RADIX = 16
val ENCRYPTION_LENGTH = 32

fun String.md5(): String {
    val md = MessageDigest.getInstance(ENCRYPTION_TYPE_MD5)
    return BigInteger(ENCRYPTION_SIGN_NUM, md.digest(toByteArray()))
        .toString(ENCRYPTION_RADIX)
        .padStart(ENCRYPTION_LENGTH, ENCRYPTION_PAD_CHAR)
}