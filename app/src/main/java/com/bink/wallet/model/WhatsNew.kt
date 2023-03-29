package com.bink.wallet.model

data class WhatsNew(
    val id: String? = null,
    val published: Boolean? = null,
    val showFrom: Int? = null,
    val adhocMessages: ArrayList<NewFeature>? = null,
    val features: ArrayList<NewFeature>? = null,
    val merchants: ArrayList<NewMerchant>? = null,
)

data class NewFeature(val description: String? = null, val imageUrl: String? = null)

data class NewMerchant(val description: String? = null, val membershipPlanId: String? = null)