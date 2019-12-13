package com.bink.wallet.model.response.membership_plan

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bink.wallet.utils.enums.CardType
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
@Entity(tableName = "membership_plan")
class MembershipPlan(
    @PrimaryKey @ColumnInfo(name = "id") var id: String,
    @ColumnInfo(name = "status") val status: String?,
    @ColumnInfo(name = "feature_set") val feature_set: FeatureSet?,
    @ColumnInfo(name = "account") val account: Account?,
    @ColumnInfo(name = "images") val images: List<Images>?,
    @ColumnInfo(name = "balances") val balances: List<Balances>?,
    @ColumnInfo(name = "has_vouchers") val has_vouchers: Boolean?
) : Parcelable {

    constructor(id: String) : this(
        id,
        null,
        null,
        null,
        null,
        null,
        null
    )

    fun getCardType(): CardType? {
        return when (feature_set?.card_type) {
            0 -> CardType.STORE
            1 -> CardType.VIEW
            2 -> CardType.PLL
            else -> null
        }
    }
}