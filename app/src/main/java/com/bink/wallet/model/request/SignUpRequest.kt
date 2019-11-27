package com.bink.wallet.model.request

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SignUpRequest(
    val bundle_id: String = "com.bink.wallet",
    val client_id: String = "MKd3FfDGBi1CIUQwtahmPap64lneCa2R6GvVWKg6dNg4w9Jnpd",
    val email: String?,
    val password: String?
)