package com.bink.wallet.model.request.membership_card

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Account(
    val add_fields: List<AddFields>,
    val authorise_fields: List<AuthoriseFields>
)