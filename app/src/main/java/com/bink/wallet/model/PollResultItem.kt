package com.bink.wallet.model

data class PollResultItem(
    val answer: String = "",
    val customAnswer: String = "",
    val createdDate: Int = 0,
    val overwritten: Boolean = false,
    val pollId: String? = null,
    val userId: String = "",
)