package com.bink.wallet.model.request

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Preference(
    val slug: String?,
    val is_user_defined: Boolean?,
    val user: Int?,
    val value: String?,
    val default_value: String?,
    val value_type: String?,
    val scheme: Int?,
    val label: String?,
    val category: String?

)