package com.bink.wallet.scenes.browse_brands.model

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class Images (

	val id : Int?,
	val type : Int?,
	val url : String?,
	val description : String?,
	val encoding : String?
) : Parcelable