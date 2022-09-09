package com.bink.wallet.model.tescolocations


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Feature(
    @Json(name = "geometry")
    val geometry: Geometry?,
    @Json(name = "properties")
    val properties: Properties?,
    @Json(name = "type")
    val type: String?
)