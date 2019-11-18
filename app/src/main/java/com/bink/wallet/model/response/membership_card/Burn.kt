package com.bink.wallet.model.response.membership_card

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class Burn(
    val type : String?,
    val value : Int?,
    val prefix : String?,
    val suffix : String?,
    val currency : String?
) : Parcelable