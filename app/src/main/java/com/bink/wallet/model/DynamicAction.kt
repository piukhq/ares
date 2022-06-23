package com.bink.wallet.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

data class DynamicAction(
    val name: String?,
    val type: DynamicActionType?,
    val start_date: Int?,
    val end_date: Int?,
    val locations: ArrayList<DynamicActionLocation>?,
    val event: DynamicActionEvent?
)

enum class DynamicActionType {
    @SerializedName("xmas")
    XMAS
}

data class DynamicActionLocation(
    val icon: String?,
    val screen: DynamicActionScreen?,
    val area: DynamicActionArea?,
    val action: DynamicActionHandler?
)

enum class DynamicActionScreen {
    @SerializedName("loyalty_wallet")
    LOYALTY_WALLET,

    @SerializedName("payment_wallet")
    PAYMENT_WALLET

}

enum class DynamicActionArea {
    @SerializedName("left_top_bar")
    LEFT_TOP_BAR
}

enum class DynamicActionHandler {
    @SerializedName("single_tap")
    SINGLE_TAP
}

@Parcelize
data class DynamicActionEvent(
    val type: DynamicActionEventType?,
    val body: DynamicActionEventBody?
) : Parcelable

enum class DynamicActionEventType {
}

@Parcelize
data class DynamicActionEventBody(
    val title: String?,
    val description: String?,
    val cta: DynamicActionEventBodyCta?
): Parcelable

@Parcelize
data class DynamicActionEventBodyCta(
    val title: String?,
    val action: DynamicActionEventBodyCTAHandler?
): Parcelable

enum class DynamicActionEventBodyCTAHandler {
    @SerializedName("zd_contact_us")
    ZENDESK_CONTACT_US
}