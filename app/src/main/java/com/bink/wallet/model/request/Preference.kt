package com.bink.wallet.model.request

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Preference(
    val is_user_defined: Boolean?,
    val user: Int?,
    val value: Int?,
    val slug: String?,
    val default_value: Int?,
    val value_type: String?,
    val scheme: String?,
    val label: String?,
    val category: String?
)