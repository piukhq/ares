package com.bink.wallet.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MagicLinkToken(val token: String)
