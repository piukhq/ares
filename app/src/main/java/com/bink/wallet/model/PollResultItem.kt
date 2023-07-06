package com.bink.wallet.model

import com.google.firebase.firestore.DocumentId

data class PollResultItem(
    @DocumentId
    val id: String? = null,
    val answer: String = "",
    val customAnswer: String = "",
    val createdDate: Int = 0,
    val overwritten: Boolean = false,
    val pollId: String? = null,
    val userId: String = "",
)