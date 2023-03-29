package com.bink.wallet.utils.firebase

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

const val FIREBASE_COLLECTION_WHATS_NEW = "whatsNew"

fun Firebase.whatsNew(): CollectionReference {
    val firestore = firestore
    return firestore.collection(FIREBASE_COLLECTION_WHATS_NEW)
}

fun getTime(): Int {
    val currentTime = System.currentTimeMillis() / 1000
    return currentTime.toInt()
}