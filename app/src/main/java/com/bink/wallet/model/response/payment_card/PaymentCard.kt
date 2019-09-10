package com.bink.wallet.model.response.payment_card

import android.os.Parcelable
import androidx.room.Entity
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize


@Parcelize
@JsonClass(generateAdapter = true)
@Entity(tableName = "payment_card")
data class PaymentCard(
    val id: String,
    val membership_cards: List<MembershipCard>,
    val status: String,
    val card: Card,
    val account: Account
) : Parcelable