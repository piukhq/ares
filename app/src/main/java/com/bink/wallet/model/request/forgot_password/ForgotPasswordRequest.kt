package com.bink.wallet.model.request.forgot_password

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ForgotPasswordRequest(
    var email: String
)