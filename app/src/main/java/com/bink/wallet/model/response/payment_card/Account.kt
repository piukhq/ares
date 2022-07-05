package com.bink.wallet.model.response.payment_card

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class Account(
    val verification_in_progress: Boolean?,
    val status: Int?,
    val consents: List<Consent>?
) : Parcelable