package com.bink.wallet.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class WhatsNew(
    val id: String? = null,
    val published: Boolean? = null,
    val showFrom: Int? = null,
    val adhocMessages: List<NewFeature>? = null,
    val features: List<NewFeature>? = null,
    val merchants: List<NewMerchant>? = null,
) : Parcelable

@Parcelize
data class NewFeature(val description: String? = null, val imageUrl: String? = null) : Parcelable

@Parcelize
data class NewMerchant(val description: String? = null, val membershipPlanId: String? = null) : Parcelable