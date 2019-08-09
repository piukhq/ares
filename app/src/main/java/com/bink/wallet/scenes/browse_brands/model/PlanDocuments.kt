package com.bink.wallet.scenes.browse_brands.model

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class PlanDocuments (

	val name : String?,
	val description : String?,
	val url : String?,
	val display : List<String>?,
	val checkbox : Boolean?
) : Parcelable