package com.bink.wallet.model.response.membership_plan

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class FeatureSet (
	val authorisation_required : Boolean?,
	val transactions_available : Boolean?,
	val digital_only : Boolean?,
	val has_points : Boolean?,
	val card_type : Int?,
	val linking_support : List<String>?,
	val apps : List<Apps>?
) : Parcelable