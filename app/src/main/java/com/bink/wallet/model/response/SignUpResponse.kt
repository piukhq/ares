package com.bink.wallet.model.response

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SignUpResponse(
    var email: String?,
    var api_key: String?,
    var uid: String?
)