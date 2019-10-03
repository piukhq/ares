package com.bink.wallet.model

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "settings_item")
enum class SettingsItemType : Parcelable {
    VERSION_NUMBER,
    BASE_URL,
    EMAIL_ADDRESS
}
