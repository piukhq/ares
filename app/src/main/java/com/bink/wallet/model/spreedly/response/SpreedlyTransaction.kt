package com.bink.wallet.model.spreedly.response

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class SpreedlyTransaction(
    val payment_method: SpreedlyResponsePaymentMethod
) : Parcelable