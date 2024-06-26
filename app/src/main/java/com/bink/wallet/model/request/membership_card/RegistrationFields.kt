package com.bink.wallet.model.request.membership_card

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RegistrationFields(
    var column: String?,
    var value: String?
)