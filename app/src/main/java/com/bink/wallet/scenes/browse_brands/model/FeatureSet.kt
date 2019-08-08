package com.bink.wallet.scenes.browse_brands.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FeatureSet (

	val authorisation_required : Boolean?,
	val transactions_available : Boolean?,
	val digital_only : Boolean?,
	val has_points : Boolean?,
	val card_type : Int?,
	val linking_support : List<String>?,
	val apps : List<Apps>?
)