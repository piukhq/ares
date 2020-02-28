package com.bink.wallet.modal.generic

import android.os.Parcelable
import androidx.annotation.DrawableRes
import kotlinx.android.parcel.Parcelize

@Parcelize
data class GenericModalParameters(
    @DrawableRes var topBarIconId: Int = 0,
    var isCloseModal: Boolean,
    var title: String,
    var description: String = "",
    var firstButtonText: String = "",
    var secondButtonText: String = "",
    var link: String = "",
    var description2: String = ""
) : Parcelable