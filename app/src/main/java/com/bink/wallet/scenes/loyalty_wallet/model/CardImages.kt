package com.bink.wallet.scenes.loyalty_wallet.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CardImages(
    var id: String?,
    var url: String?,
    var type: Int?,
    var description: String?,
    var encoding: String?
)