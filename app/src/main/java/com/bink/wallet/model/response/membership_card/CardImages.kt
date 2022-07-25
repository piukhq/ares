package com.bink.wallet.model.response.membership_card

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class CardImages(
    var id: String?,
    var url: String?,
    var type: Int?,
    var description: String?,
    var encoding: String?
) : Parcelable