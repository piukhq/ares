package com.bink.wallet.scenes.loyalty_wallet.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CardBalance(
    var value: String,
    var curency: String,
    var prefix: String,
    var suffix: String,
    var updated_at: Int
)