package com.bink.wallet.model.request.membership_card

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Account(
    var add_fields: MutableList<AddFields>?,
    var authorise_fields: MutableList<AuthoriseFields>?
)