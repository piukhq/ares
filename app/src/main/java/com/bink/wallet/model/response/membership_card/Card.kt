package com.bink.wallet.model.response.membership_card

import android.graphics.Color
import android.os.Parcelable
import androidx.room.ColumnInfo
import com.bink.wallet.utils.ColorUtil
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class Card(
    @ColumnInfo(name = "barcode") var barcode: String?,
    @ColumnInfo(name = "barcode_type") var barcode_type: Int?,
    @ColumnInfo(name = "membership_id") var membership_id: String?,
    @ColumnInfo(name = "colour") var colour: String?
) : Parcelable {

    fun getSecondaryColor(): String {
        val primaryColor = Color.parseColor(colour)
        if (ColorUtil.isColorLight(primaryColor)) {
            return ColorUtil.darkenColor(primaryColor)
        } else {
            return ColorUtil.lightenColor(primaryColor)
        }
    }

}