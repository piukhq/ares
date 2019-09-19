package com.bink.wallet.model.response.membership_card

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class CardBalance(
    var value: String?,
    var curency: String?,
    var prefix: String?,
    var suffix: String?,
    var updated_at: Long?
) : Parcelable