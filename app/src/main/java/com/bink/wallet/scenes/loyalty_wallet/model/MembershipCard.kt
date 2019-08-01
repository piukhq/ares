package com.bink.wallet.scenes.loyalty_wallet.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MembershipCard(
    var id: String,
    var membership_plan: String,
    var cardStatus: CardStatus,
    var card: Card,
    var images: MutableList<CardImages>,
    var cardBalances: MutableList<CardBalance>
)