package com.bink.wallet.model.spreedly

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class SpreedlyPaymentMethod(
    val credit_card: SpreedlyCreditCard,
    val retained: String
) : Parcelable