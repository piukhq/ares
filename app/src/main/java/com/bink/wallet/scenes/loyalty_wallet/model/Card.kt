package com.bink.wallet.scenes.loyalty_wallet.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Card(
    var barcode: String?,
    var barcode_type: Int?,
    var membership_id: String?,
    var colour: String?
)