package com.bink.wallet.model

import android.os.Parcelable
import androidx.room.Entity
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "settings_item")
enum class SettingsItemType : Parcelable {
    HEADER,
    LOGOUT,
    PREFERENCES,
    FAQS,
    CONTACT_US,
    RATE_APP,
    DELETE_ACC,
    SECURITY_AND_PRIVACY,
    HOW_IT_WORKS,
    WHO_WE_ARE,
    PRIVACY_POLICY,
    TERMS_AND_CONDITIONS,
    DEBUG_MENU,
    FOOTER
}
