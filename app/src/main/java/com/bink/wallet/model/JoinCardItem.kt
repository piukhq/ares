package com.bink.wallet.model


import android.os.Parcelable
import com.bink.wallet.utils.JOIN_CARD
import kotlinx.parcelize.Parcelize

@Parcelize
class JoinCardItem(
    var id: String = JOIN_CARD
) : Parcelable