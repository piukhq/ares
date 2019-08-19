package com.bink.wallet.scenes.browse_brands.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
@Entity(tableName = "membership_plan")
data class MembershipPlan(
    @PrimaryKey @ColumnInfo(name = "id")val id: Int?,
    @ColumnInfo(name = "status") val status: String?,
    @ColumnInfo(name = "feature_set") val feature_set: FeatureSet?,
    @ColumnInfo(name = "account") val account: Account?,
    @ColumnInfo(name = "images") val images: List<Images>?,
    @ColumnInfo(name = "balances") val balances: List<Balances>?
) : Parcelable