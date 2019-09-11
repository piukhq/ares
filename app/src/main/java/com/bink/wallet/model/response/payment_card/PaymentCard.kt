package com.bink.wallet.model.response.payment_card

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class PaymentCard(
    val id: Int?,
    val membership_cards: List<PaymentMembershipCard>?,
    val status: String?,
    val bankCard: BankCard?,
    val images: List<Image>?,
    val account: Account?
) : Parcelable