package com.bink.wallet.model.request.membership_card

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AddFields(

    val column: String,
    val value: String
)