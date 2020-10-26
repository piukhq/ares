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
    val headline : String?,
    val code : String?,
    val date_issued : Long?,
    val expiry_date : Long?,
    val date_redeemed : Long?,
    val barcode_type : Int?
) : Parcelable