package com.bink.wallet.scenes.browse_brands.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class EnrolFields (

	val column : String?,
	val validation : String?,
	val common_name : String?,
	val type : Int?,
	val choice : List<String>?
)