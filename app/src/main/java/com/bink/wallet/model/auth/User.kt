package com.bink.wallet.model.auth

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class User(val first_name: String, val last_name: String, var uid: String = "")