package com.bink.wallet.model.response.membership_card

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class Amounts(
    val value: Double?,
    val currency: String?,
    val suffix: String?,
    val prefix: String?
) : Parcelable