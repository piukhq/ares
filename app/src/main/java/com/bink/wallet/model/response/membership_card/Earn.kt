package com.bink.wallet.model.response.membership_card

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class Earn(
    val type : String?,
    val value : Float?,
    val target_value: Float?,
    val prefix : String?,
    val suffix : String?,
    val currency : String?
) : Parcelable