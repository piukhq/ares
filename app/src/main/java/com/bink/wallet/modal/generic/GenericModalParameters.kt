package com.bink.wallet.modal.generic

import android.os.Parcelable
import androidx.annotation.DrawableRes
import kotlinx.android.parcel.Parcelize

@Parcelize
data class GenericModalParameters(
    @DrawableRes var topBarIconId: Int = 0,
    var title: String,
    var description: String,
    var description2: String = "",
    var firstButtonText: String = "",
    var secondButtonText: String = "",
    var link: String = ""
) : Parcelable