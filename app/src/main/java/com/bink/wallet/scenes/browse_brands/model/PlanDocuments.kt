package com.bink.wallet.scenes.browse_brands.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PlanDocuments (

	val name : String?,
	val description : String?,
	val url : String?,
	val display : List<String>?,
	val checkbox : Boolean?
)