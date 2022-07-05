package com.bink.wallet.model

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class PostServiceRequest(val consent: Consent) : Parcelable

@Parcelize
@JsonClass(generateAdapter = true)
data class Consent(val email: String, val timestamp: Long) : Parcelable