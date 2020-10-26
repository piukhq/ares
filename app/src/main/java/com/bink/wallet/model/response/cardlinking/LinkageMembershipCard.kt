package com.bink.wallet.model.response.cardlinking


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LinkageMembershipCard(
    @Json(name = "id")
    val id: Int,
    @Json(name = "link_active")
    val linkActive: Boolean
)