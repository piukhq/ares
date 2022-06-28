package com.bink.wallet.utils.local_point_scraping

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class WebScrapeCredentials(
    val email: String?,
    val password: String?,
    val cardId: String?
) : Parcelable