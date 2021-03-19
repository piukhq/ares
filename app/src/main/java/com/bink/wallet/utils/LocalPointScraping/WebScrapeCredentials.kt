package com.bink.wallet.utils.LocalPointScraping

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class WebScrapeCredentials(
    val email: String?,
    val password: String?,
    val cardId: String?
) : Parcelable