package com.bink.wallet.model.response.membership_card

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class Voucher(
    val burn : Burn?,
    val earn : Earn?,
    val state : String?,
    val subtext : String?,
    val headline : String?
) : Parcelable
