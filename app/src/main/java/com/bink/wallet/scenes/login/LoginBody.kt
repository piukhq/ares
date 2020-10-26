package com.bink.wallet.scenes.login

import com.squareup.moshi.JsonClass

/**
 */
@JsonClass(generateAdapter = true)
data class LoginBody(
    val timestamp: Long = -1,
    val email: String,
    val latitude: Double,
    val longitude: Double
)