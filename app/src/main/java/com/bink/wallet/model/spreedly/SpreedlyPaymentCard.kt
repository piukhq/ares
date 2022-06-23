package com.bink.wallet.model.spreedly

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class SpreedlyPaymentCard(
    val payment_method: SpreedlyPaymentMethod
) : Parcelable