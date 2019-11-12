package com.bink.wallet.model.response.membership_card

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.bink.wallet.model.response.membership_plan.MembershipPlan
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
    @ColumnInfo(name = "status") var status: CardStatus?,
    @ColumnInfo(name = "bankCard") var card: Card?,
    @ColumnInfo(name = "card_images") var images: List<CardImages>?,
    @ColumnInfo(name = "balances") var balances: List<CardBalance>?,
    @ColumnInfo(name = "membership_transactions") var membership_transactions: List<MembershipTransactions>?
) : Parcelable {
    companion object {
        val RESPONSE_LINKED = "linked"
        val RESPONSE_LINK_NOW = "link_now"
        val RESPONSE_RETRY = "retry"
        val RESPONSE_PENDING = "pending"
    }
    @Ignore
    var plan: MembershipPlan? = null

    constructor(id: String) : this(
        id,
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

    fun getLinkStatus(): String {
        if (plan?.feature_set?.card_type in 0..1) {
            return ""
        }
        return when (status?.state) {
            com.bink.wallet.utils.enums.CardStatus.AUTHORISED.status -> {
                val pc = payment_cards
                if (pc == null ||
                    pc.isEmpty()
                ) {
                    RESPONSE_LINK_NOW
                } else {
                    RESPONSE_LINKED
                }
            }

            com.bink.wallet.utils.enums.CardStatus.UNAUTHORISED.status,
            com.bink.wallet.utils.enums.CardStatus.FAILED.status ->
                RESPONSE_RETRY

            com.bink.wallet.utils.enums.CardStatus.PENDING.status ->
                RESPONSE_PENDING

            else -> ""
        }
    }
}