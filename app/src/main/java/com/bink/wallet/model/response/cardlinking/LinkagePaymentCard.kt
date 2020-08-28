package com.bink.wallet.model.response.cardlinking


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LinkagePaymentCard(
    @Json(name = "country")
    val country: String,
    @Json(name = "currency_code")
    val currencyCode: String,
    @Json(name = "first_six_digits")
    val firstSixDigits: Int,
    @Json(name = "last_four_digits")
    val lastFourDigits: Int,
    @Json(name = "month")
    val month: Int,
    @Json(name = "name_on_card")
    val nameOnCard: String,
    @Json(name = "provider")
    val provider: String,
    @Json(name = "type")
    val type: String,
    @Json(name = "year")
    val year: Int
)