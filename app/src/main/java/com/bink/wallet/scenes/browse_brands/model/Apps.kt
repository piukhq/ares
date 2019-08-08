package com.bink.wallet.scenes.browse_brands.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Apps (

	val app_store_url : String?,
	val app_type : Int?
)