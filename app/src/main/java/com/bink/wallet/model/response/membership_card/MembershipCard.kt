package com.bink.wallet.model.response.membership_card

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
@Entity(tableName = "membership_card")
class MembershipCard(
    @PrimaryKey @ColumnInfo(name = "id") var id: String,
    @ColumnInfo(name = "membership_plan") var membership_plan: String?,
    @ColumnInfo(name = "card_status") var status: CardStatus?,
    @ColumnInfo(name = "card") var card: Card?,
    @ColumnInfo(name = "card_images") var images: List<CardImages>?,
    @ColumnInfo(name = "balances") var balances: List<CardBalance>?,
    @ColumnInfo(name = "membership_transactions") var membership_transactions: List<MembershipTransactions>?
) : Parcelable {

    fun getHeroImage(): CardImages? {
        return images?.first { image -> image.type == 0 }
    }
}