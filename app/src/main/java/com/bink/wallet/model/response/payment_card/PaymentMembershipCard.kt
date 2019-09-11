package com.bink.wallet.model.response.payment_card

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class PaymentMembershipCard(val id: Int, val active_link: Boolean): Parcelable