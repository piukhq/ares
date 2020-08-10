package com.bink.wallet.model.response.membership_plan


import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class Content(
    @Json(name = "column")
    val column: String?,
    @Json(name = "value")
    val value: String?
):Parcelable