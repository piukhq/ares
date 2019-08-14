package com.bink.wallet.scenes.loyalty_wallet.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
@Entity(tableName = "membership_card")
data class MembershipCard(
    @PrimaryKey  var id: String,
    @ColumnInfo(name = "membership_plan") var membership_plan: String?,
    @ColumnInfo(name = "card_status") var cardStatus: CardStatus?,
    @ColumnInfo(name = "card") var card: Card?,
    @ColumnInfo(name = "card_images") var images: MutableList<CardImages>?,
    @ColumnInfo(name = "card_balances") var cardBalances: MutableList<CardBalance>?
) : Parcelable