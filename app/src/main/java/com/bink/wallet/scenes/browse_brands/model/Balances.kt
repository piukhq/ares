package com.bink.wallet.scenes.browse_brands.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Balances (

	val currency : String?,
	val suffix : String?,
	val description : String?
)