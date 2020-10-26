package com.bink.wallet.model.request.membership_card

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MembershipCardRequest(
    val account: Account?,
    val membership_plan: String?
)