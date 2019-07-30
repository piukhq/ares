package com.bink.wallet.scenes.login

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginResponse(
    val consent: LoginBody
)
