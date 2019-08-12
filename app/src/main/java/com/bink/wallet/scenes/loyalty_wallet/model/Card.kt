package com.bink.wallet.scenes.loyalty_wallet.model

import android.os.Parcel
import android.os.Parcelable
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Card(
    var barcode: String?,
    var barcode_type: Int?,
    var membership_id: String?,
    var colour: String?
):Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel?, p1: Int) {
        parcel?.let{
            it.writeString(barcode)
            barcode_type?.let { it1 -> it.writeInt(it1) }
            it.writeString(membership_id)
            it.writeString(colour)
        }
    }

    override fun describeContents(): Int  = 0

    companion object CREATOR : Parcelable.Creator<Card> {
        override fun createFromParcel(parcel: Parcel): Card {
            return Card(parcel)
        }

        override fun newArray(size: Int): Array<Card?> {
            return arrayOfNulls(size)
        }
    }
}