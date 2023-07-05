package com.bink.wallet.model.locations


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Geometry(
    @Json(name = "coordinates")
    val coordinates: List<Double>?,
    @Json(name = "type")
    val type: String?
)