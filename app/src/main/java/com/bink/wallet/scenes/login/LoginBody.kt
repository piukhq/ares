package com.bink.wallet.scenes.login

import com.squareup.moshi.JsonClass

/**
 */
@JsonClass(generateAdapter = true)
data class LoginBody(
    val timestamp: Int = -1,
    val email: String
)