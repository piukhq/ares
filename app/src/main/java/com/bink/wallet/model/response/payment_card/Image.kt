package com.bink.wallet.model.response.payment_card

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class Image(val id: Int?, val type: Int?, val url: String?, val description: String?, val encoding: String?): Parcelable