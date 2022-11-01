package com.bink.wallet.model.tescolocations


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TescoLocation(
    @Json(name = "features")
    val features: List<Feature>?,
    @Json(name = "type")
    val type: String?
)