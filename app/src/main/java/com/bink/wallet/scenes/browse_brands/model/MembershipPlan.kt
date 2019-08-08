package com.bink.wallet.scenes.browse_brands.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MembershipPlan (

	val id : Int?,
	val status : String?,
	val feature_set : FeatureSet?,
	val account : Account?,
	val images : List<Images>?,
	val balances : List<Balances>?
)