package com.bink.wallet.utils.firebase

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

const val FIREBASE_COLLECTION_WHATS_NEW = "whatsNew"
const val FIREBASE_COLLECTION_POLLS = "polls"

fun Firebase.whatsNew(): CollectionReference {
    return firestore.collection(FIREBASE_COLLECTION_WHATS_NEW)
}

fun Firebase.polls(): CollectionReference {
    val firestore = firestore
    return firestore.collection(FIREBASE_COLLECTION_POLLS)
}

fun getTime(): Int {
    val currentTime = System.currentTimeMillis() / 1000
    return currentTime.toInt()
}

