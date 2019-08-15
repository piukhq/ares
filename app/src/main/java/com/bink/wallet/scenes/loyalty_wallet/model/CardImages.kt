package com.bink.wallet.scenes.loyalty_wallet.model

import android.os.Parcel
import android.os.Parcelable
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CardImages(
    var id: String?,
    var url: String?,
    var type: Int?,
    var description: String?,
    var encoding: String?
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        parcel.readString()
    )
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(url)
        parcel.writeValue(type)
        parcel.writeString(description)
        parcel.writeString(encoding)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CardImages> {
        override fun createFromParcel(parcel: Parcel): CardImages {
            return CardImages(parcel)
        }

        override fun newArray(size: Int): Array<CardImages?> {
            return arrayOfNulls(size)
        }
    }
}