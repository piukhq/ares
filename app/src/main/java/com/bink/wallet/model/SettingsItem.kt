package com.bink.wallet.model

import android.os.Parcelable
import androidx.room.Entity
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "settings_item")
data class SettingsItem(
    val title: String?,
    val value: String?,
    val type: SettingsItemType,
    val url: String?
) : Parcelable