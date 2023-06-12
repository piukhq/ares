package com.bink.wallet.model

data class PollItem(
    val id: String? = null,
    val question: String = "",
    val title: String = "",
    val startTime: Int = 0,
    val closeTime: Int? = null,
    val published: Boolean = false,
    val allowCustomAnswer: Boolean = false,
    val answers: List<String>? = null,
)