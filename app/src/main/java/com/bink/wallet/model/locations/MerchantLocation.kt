package com.bink.wallet.model.locations


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MerchantLocation(
    @Json(name = "features")
    val features: List<Feature>?,
    @Json(name = "type")
    val type: String?
)