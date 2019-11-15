package com.bink.wallet.model.auth

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FacebookAuthRequest(val access_token: String, val email: String, val userId: String)