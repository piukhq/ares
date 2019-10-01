package com.bink.wallet.modal.generic

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.DrawableRes

data class GenericModalParameters(
    @DrawableRes var topBarIconId: Int,
    var title: String,
    var description: String
) : Parcelable {
    constructor(
        topBarIconId: Int,
        title: String,
        description: String,
        firstButtonText: String,
        secondButtonText: String
    ) : this(topBarIconId, title, description) {
        this.firstButtonText = firstButtonText
        this.secondButtonText = secondButtonText
    }

    var firstButtonText = ""
    var secondButtonText = ""
    var joinUnavailableLink = ""

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readString()!!
    ) {
        firstButtonText = parcel.readString()!!
        secondButtonText = parcel.readString()!!
        joinUnavailableLink = parcel.readString()!!
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(topBarIconId)
        parcel.writeString(title)
        parcel.writeString(description)
        parcel.writeString(firstButtonText)
        parcel.writeString(secondButtonText)
        parcel.writeString(joinUnavailableLink)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<GenericModalParameters> {
        override fun createFromParcel(parcel: Parcel): GenericModalParameters {
            return GenericModalParameters(parcel)
        }

        override fun newArray(size: Int): Array<GenericModalParameters?> {
            return arrayOfNulls(size)
        }
    }
}