package com.bink.wallet.model.response.payment_card

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class Consent(
    val type: Int?,
    val latitude: Float?,
    val longitude: Float?,
    val timestamp: Long?
) : Parcelable