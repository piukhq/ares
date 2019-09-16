package com.bink.wallet.model.response.membership_plan

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class EnrolFields (
	val description: String?,
	val column : String?,
	val validation : String?,
	val common_name : String?,
	val type : Int?,
	val choice : List<String>?
) : Parcelable