package com.bink.wallet.model.response.payment_card

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class BankCard(
    val first_six_digits: String?,
    val last_four_digits: String?,
    val month: Int?,
    val year: Int?,
    val country: String?,
    val currency_code: String?,
    val name_on_card: String?,
    val provider: String?,
    val type: String?
) : Parcelable