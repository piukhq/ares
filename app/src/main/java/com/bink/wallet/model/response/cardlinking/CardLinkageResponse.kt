package com.bink.wallet.model.response.cardlinking


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CardLinkageResponse(
    @Json(name = "card")
    val linkagePaymentCard: LinkagePaymentCard,
    @Json(name = "id")
    val id: Long,
    @Json(name = "membership_cards")
    val linkageMembershipCards: List<LinkageMembershipCard>,
    @Json(name = "status")
    val status: String
)