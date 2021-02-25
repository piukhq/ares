package com.bink.wallet.utils.LocalPointScraping

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class WebScrapeCredentials(
    @ColumnInfo(name = "email") val email: String?,
    @ColumnInfo(name = "password") val password: String?
) : Parcelable