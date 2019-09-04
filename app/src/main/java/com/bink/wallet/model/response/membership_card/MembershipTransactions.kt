package com.bink.wallet.model.response.membership_card

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class MembershipTransactions (
	val description : String?,
	val timestamp : Long?,
	val amounts : List<Amounts>?
) : Parcelable