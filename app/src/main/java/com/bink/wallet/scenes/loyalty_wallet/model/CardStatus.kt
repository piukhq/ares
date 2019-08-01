package com.bink.wallet.scenes.loyalty_wallet.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CardStatus(
    var reason_codes: MutableList<String>,
    var state: String
)