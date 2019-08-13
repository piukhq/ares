package com.bink.wallet.scenes.loyalty_wallet.model

import android.os.Parcel
import android.os.Parcelable
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CardStatus(
    var reason_codes: MutableList<String>?,
    var state: String?
):Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.createStringArrayList(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeStringList(reason_codes)
        parcel.writeString(state)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CardStatus> {
        override fun createFromParcel(parcel: Parcel): CardStatus {
            return CardStatus(parcel)
        }

        override fun newArray(size: Int): Array<CardStatus?> {
            return arrayOfNulls(size)
        }
    }
}