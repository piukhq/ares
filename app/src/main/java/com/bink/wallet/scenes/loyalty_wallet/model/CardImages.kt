package com.bink.wallet.scenes.loyalty_wallet.model

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class CardImages(
    var id: String?,
    var url: String?,
    var type: Int?,
    var description: String?,
    var encoding: String?
): Parcelable