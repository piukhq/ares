package com.bink.wallet.scenes.loyalty_wallet.model

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class CardStatus(
    var reason_codes: MutableList<String>?,
    var state: String?
):Parcelable