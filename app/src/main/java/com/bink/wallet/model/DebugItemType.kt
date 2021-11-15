package com.bink.wallet.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
enum class DebugItemType : Parcelable {
    CURRENT_VERSION,
    ENVIRONMENT,
    EMAIL,
    BACKEND_VERSION,
    COLOR_SWATCHES,
    FORCE_CRASH,
    TESCO_LPS,
    CARD_ON_BOARDING,
    BARCODE_FROM_IMAGE
}