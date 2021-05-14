package com.bink.wallet.model.response.membership_plan

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class Images(
    val id: Int?,
    val type: Int?,
    val url: String?,
    val description: String?,
    val encoding: String?,
    val cta_url: String?
) : Parcelable