package com.bink.wallet.model.request

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MarketingOption(
    @Json(name = "marketing-bink") val marketing: Int
)
