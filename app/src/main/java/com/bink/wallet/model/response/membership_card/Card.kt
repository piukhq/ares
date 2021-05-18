package com.bink.wallet.model.response.membership_card

import android.graphics.Color
import android.os.Parcelable
import androidx.room.ColumnInfo
import com.bink.wallet.utils.ColorUtil
import com.google.zxing.BarcodeFormat
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class Card(
    @ColumnInfo(name = "barcode") var barcode: String?,
    @ColumnInfo(name = "barcode_type") var barcode_type: Int?,
    @ColumnInfo(name = "membership_id") var membership_id: String?,
    @ColumnInfo(name = "colour") var colour: String?,
    @ColumnInfo(name = "secondary_colour") var secondary_colour: String?
) : Parcelable {

    fun getSecondaryColor(): String {
        secondary_colour?.let { return it }

        val primaryColor = Color.parseColor(colour)
        return if (ColorUtil.isColorLight(primaryColor)) {
            ColorUtil.darkenColor(primaryColor)
        } else {
            ColorUtil.lightenColor(primaryColor)
        }
    }

    fun getBarcodeFormat() : BarcodeFormat?{
        return when (barcode_type) {
            0, null ->  BarcodeFormat.CODE_128
            1 -> BarcodeFormat.QR_CODE
            2 -> BarcodeFormat.AZTEC
            3 -> BarcodeFormat.PDF_417
            4 -> BarcodeFormat.EAN_13
            5 -> BarcodeFormat.DATA_MATRIX
            6 -> BarcodeFormat.ITF
            7 -> BarcodeFormat.CODE_39
            else -> null
        }
    }

}