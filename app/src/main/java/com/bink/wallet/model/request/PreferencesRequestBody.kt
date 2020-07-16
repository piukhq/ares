package com.bink.wallet.model.request

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PreferencesRequestBody(@Json(name = "marketing-bink") val marketing_bink: Int)
