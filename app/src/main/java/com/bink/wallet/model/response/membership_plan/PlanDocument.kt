package com.bink.wallet.model.response.membership_plan

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class PlanDocument(
    val name: String?,
    val description: String?,
    val url: String?,
    val display: List<String>?,
    val checkbox: Boolean?
) : Parcelable