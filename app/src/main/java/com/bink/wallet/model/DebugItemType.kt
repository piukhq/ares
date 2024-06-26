package com.bink.wallet.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class DebugItemType : Parcelable {
    CURRENT_VERSION,
    ENVIRONMENT,
    EMAIL,
    BACKEND_VERSION,
    COLOR_SWATCHES,
    FORCE_CRASH,
    CARD_ON_BOARDING,
    RESET_CACHE,
    EXPORT_NETWORK,
    CURRENT_TOKEN
}