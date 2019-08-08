package com.bink.wallet.scenes.browse_brands.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Images (

	val id : Int?,
	val type : Int?,
	val url : String?,
	val description : String?,
	val encoding : String?
)