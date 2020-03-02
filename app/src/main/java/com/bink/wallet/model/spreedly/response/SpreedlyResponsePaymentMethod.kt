package com.bink.wallet.model.spreedly.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class SpreedlyResponsePaymentMethod(
    val token: String,
    val fingerprint: String,
    val first_six_digits: String,
    val last_four_digits: String
) : Parcelable