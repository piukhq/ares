package com.bink.wallet.model.auth

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FacebookAuthResponse(val email: String, val api_key: String, val uid: String)