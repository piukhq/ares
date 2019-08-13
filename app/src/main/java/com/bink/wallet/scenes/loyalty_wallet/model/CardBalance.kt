package com.bink.wallet.scenes.loyalty_wallet.model

import android.os.Parcel
import android.os.Parcelable
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CardBalance(
    var value: String?,
    var curency: String?,
    var prefix: String?,
    var suffix: String?,
    var updated_at: Int?
):Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(value)
        parcel.writeString(curency)
        parcel.writeString(prefix)
        parcel.writeString(suffix)
        parcel.writeValue(updated_at)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CardBalance> {
        override fun createFromParcel(parcel: Parcel): CardBalance {
            return CardBalance(parcel)
        }

        override fun newArray(size: Int): Array<CardBalance?> {
            return arrayOfNulls(size)
        }
    }
}