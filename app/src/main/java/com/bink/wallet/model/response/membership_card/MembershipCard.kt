package com.bink.wallet.model.response.membership_card

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.model.response.payment_card.PaymentMembershipCard
import com.bink.wallet.utils.enums.LoyaltyCardLinkStatus
import com.bink.wallet.utils.enums.MembershipCardStatus
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

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
    @ColumnInfo(name = "membership_transactions") var membership_transactions: List<MembershipTransactions>?,
    @ColumnInfo(name = "account") var account: Account?,
    @ColumnInfo(name = "vouchers") var vouchers: List<Voucher>?,
    @ColumnInfo(name = "uuid") var uuid: String? = null,
    @ColumnInfo(name = "isScraped") var isScraped: Boolean? = false
) : Parcelable {
    @IgnoredOnParcel
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
        null,
        null,
        null,
        null,
        null
    )

    fun getHeroImage(): CardImages? {
        return if (!images.isNullOrEmpty()) {
            images?.firstOrNull { image -> image.type == ImageType.HERO_IMAGE.value }
        } else null
    }

    fun getTierImage(): CardImages? {
        return if (!images.isNullOrEmpty()) {
            images?.firstOrNull { image -> image.type == ImageType.TIER_IMAGE.value }
        } else null
    }

    fun isAuthorised(): Boolean {
        return status?.state == MembershipCardStatus.AUTHORISED.status
    }

    fun getLinkStatus(): LoyaltyCardLinkStatus {
        if (plan?.feature_set?.card_type in 0..1) {
            return LoyaltyCardLinkStatus.NONE
        }
        return when (status?.state) {
            MembershipCardStatus.AUTHORISED.status -> {
                val linkedPaymentCards =
                    payment_cards?.filter { paymentCard -> paymentCard.active_link == true }
                if (linkedPaymentCards.isNullOrEmpty()) {
                    LoyaltyCardLinkStatus.LINK_NOW
                } else {
                    LoyaltyCardLinkStatus.LINKED
                }
            }

            MembershipCardStatus.UNAUTHORISED.status,
            MembershipCardStatus.FAILED.status ->
                LoyaltyCardLinkStatus.RETRY

            MembershipCardStatus.PENDING.status ->
                LoyaltyCardLinkStatus.PENDING

            else -> LoyaltyCardLinkStatus.NONE
        }
    }
}