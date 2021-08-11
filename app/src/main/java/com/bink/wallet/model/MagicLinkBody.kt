package com.bink.wallet.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MagicLinkBody(val email: String, val slug: String, val locale: String, val bundle_id: String)
