package com.bink.wallet.model.response.membership_card

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class Earn(
    val type : String?,
    val value : Float?,
    val target_value: Float?
) : Parcelable