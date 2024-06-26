package com.bink.wallet.model.response.payment_card

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
@JsonClass(generateAdapter = true)
@Entity(tableName = "payment_card")
data class PaymentCard(
    @PrimaryKey @ColumnInfo(name = "id") val id: Int?,
    @ColumnInfo(name = "membership_cards") val membership_cards: List<PaymentMembershipCard> = ArrayList(),
    @ColumnInfo(name = "status") val status: String?,
    @ColumnInfo(name = "card") val card: BankCard?,
    @ColumnInfo(name = "image") val images: List<Image>?,
    @ColumnInfo(name = "account") val account: Account?,
    @ColumnInfo(name = "uuid") var uuid: String? = null
) : Parcelable {
    @IgnoredOnParcel
    @Ignore
    var isSelected: Boolean = false

    fun addPaymentCard(cardId: String) {
        (membership_cards as ArrayList<PaymentMembershipCard>).add(
            PaymentMembershipCard(
                cardId,
                false
            )
        )
    }

    fun isCardActive(): Boolean {
        return status?.lowercase(Locale.getDefault()) == "active"
    }
}