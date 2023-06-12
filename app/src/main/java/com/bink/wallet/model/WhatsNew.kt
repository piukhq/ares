package com.bink.wallet.model

import android.os.Parcelable
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import kotlinx.parcelize.Parcelize

@Parcelize
data class WhatsNew(
    val id: String? = null,
    val published: Boolean? = null,
    val showFrom: Int? = null,
    val adhocMessages: List<AdHocMessage>? = null,
    val features: List<NewFeature>? = null,
    val merchants: List<NewMerchant>? = null,
) : Parcelable

fun WhatsNew.asAnyList(): ArrayList<Any> {
    val list = arrayListOf<Any>()
    merchants?.let { list.addAll(it) }
    features?.let { list.addAll(it) }
    adhocMessages?.let { list.addAll(it) }
    return list
}

@Parcelize
data class NewFeature(val description: String? = null, val imageUrl: String? = null, val title: String? = null, val screen: Int? = null) : Parcelable

@Parcelize
data class AdHocMessage(val description: String? = null, val imageUrl: String? = null, val title: String? = null, val screen: Int? = null) : Parcelable

@Parcelize
data class NewMerchant(val description: String? = null, val membershipPlanId: String? = null, var membershipPlan: MembershipPlan? = null) : Parcelable