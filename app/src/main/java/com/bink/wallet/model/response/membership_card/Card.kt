package com.bink.wallet.model.response.membership_card

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class Card(
    @ColumnInfo(name = "barcode") var barcode: String?,
    @ColumnInfo(name = "barcode_type") var barcode_type: Int?,
    @ColumnInfo(name = "membership_id") var membership_id: String?,
    @ColumnInfo(name = "colour") var colour: String?
) : Parcelable