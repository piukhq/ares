package com.bink.wallet.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PollItem(
    val id: String? = null,
    val question: String = "",
    val title: String = "",
    val startTime: Int = 0,
    val closeTime: Int? = null,
    val editTimeLimit: Int = 0,
    val published: Boolean = false,
    val allowCustomAnswer: Boolean = false,
    val answers: List<String>? = null,
) : Parcelable