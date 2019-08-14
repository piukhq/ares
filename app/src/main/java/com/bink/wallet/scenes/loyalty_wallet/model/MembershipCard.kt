package com.bink.wallet.scenes.loyalty_wallet.model
import android.os.Parcel
import android.os.Parcelable
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MembershipCard(
    var id: String?,
    var membership_plan: String?,
    var cardStatus: CardStatus?,
    var card: Card?,
    var images: MutableList<CardImages>?,
    var cardBalances: MutableList<CardBalance>?
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readParcelable(CardStatus::class.java.classLoader),
        parcel.readParcelable(Card::class.java.classLoader),
        parcel.readParcelableList(mutableListOf<CardImages>() , CardImages::class.java.classLoader),
        parcel.readParcelableList(mutableListOf<CardBalance>(), CardBalance::class.java.classLoader)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(membership_plan)
        parcel.writeParcelable(cardStatus,0)
        parcel.writeParcelable(card,0)
        parcel.writeParcelableList(images,0)
        parcel.writeParcelableList(cardBalances,0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MembershipCard> {
        override fun createFromParcel(parcel: Parcel): MembershipCard {
            return MembershipCard(parcel)
        }

        override fun newArray(size: Int): Array<MembershipCard?> {
            return arrayOfNulls(size)
        }
    }

}