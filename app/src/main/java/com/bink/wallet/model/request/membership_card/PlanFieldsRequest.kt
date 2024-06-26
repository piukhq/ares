package com.bink.wallet.model.request.membership_card

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PlanFieldsRequest(
    var column: String?,
    var value: String?,
    var disabled: Boolean? = null,
    var common_name: String? = "",
    @Transient var shouldIgnore: Boolean = false,
    @Transient var isSensitive: Boolean = false
)