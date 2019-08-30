package com.bink.wallet.model.response.membership_plan

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