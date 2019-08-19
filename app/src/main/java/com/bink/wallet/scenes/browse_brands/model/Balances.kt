package com.bink.wallet.scenes.browse_brands.model

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class Balances (
	val currency : String?,
	val suffix : String?,
	val description : String?
) : Parcelable