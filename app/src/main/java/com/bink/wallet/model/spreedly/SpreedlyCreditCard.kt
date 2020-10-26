package com.bink.wallet.model.spreedly

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class SpreedlyCreditCard(
    val number: String?,
    val month: String?,
    val year: String?,
    val full_name: String?
) : Parcelable