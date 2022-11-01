package com.bink.wallet.model.tescolocations


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Properties(
    @Json(name = "city")
    val city: String?,
    @Json(name = "latitude")
    val latitude: String?,
    @Json(name = "location_name")
    val locationName: String?,
    @Json(name = "longitude")
    val longitude: String?,
    @Json(name = "open_hours")
    val openHours: String?,
    @Json(name = "phone_number")
    val phoneNumber: String?,
    @Json(name = "postal_code")
    val postalCode: String?,
    @Json(name = "region")
    val region: String?,
    @Json(name = "street_address")
    val streetAddress: String?
)