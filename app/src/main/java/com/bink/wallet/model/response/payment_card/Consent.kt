package com.bink.wallet.model.response.payment_card

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class Consent(
    val type: Int,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long
) : Parcelable