package com.bink.wallet.model.response.membership_card

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bink.wallet.model.response.payment_card.PaymentMembershipCard
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
@Entity(tableName = "membership_card")
data class MembershipCard(
    @PrimaryKey @ColumnInfo(name = "id") var id: String,
    @ColumnInfo(name = "membership_plan") var membership_plan: String?,
    @ColumnInfo(name = "payment_cards") var payment_cards: List<PaymentMembershipCard>?,
    @ColumnInfo(name = "card_status") var status: CardStatus?,
    @ColumnInfo(name = "bankCard") var card: Card?,
    @ColumnInfo(name = "card_images") var images: List<CardImages>?,
    @ColumnInfo(name = "balances") var balances: List<CardBalance>?,
    @ColumnInfo(name = "membership_transactions") var membership_transactions: List<MembershipTransactions>?,
    @ColumnInfo(name = "account") var account: Account?,
    @ColumnInfo(name = "vouchers") var vouchers: List<Voucher>?
) : Parcelable {

    constructor(id: String) : this(
        id,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null
    )

    fun getHeroImage(): CardImages? {
        return if (!images.isNullOrEmpty()) {
            images?.firstOrNull { image -> image.type == 0 }
        } else null
    }
}