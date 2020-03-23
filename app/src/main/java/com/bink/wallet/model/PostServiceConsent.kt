package com.bink.wallet.model

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class PostServiceConsent(val consent: Consent) : Parcelable

@Parcelize
@JsonClass(generateAdapter = true)
data class Consent(val email: String, val timestamp: Long) : Parcelable