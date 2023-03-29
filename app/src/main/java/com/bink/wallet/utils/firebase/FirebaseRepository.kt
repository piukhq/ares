package com.bink.wallet.utils.firebase

import com.google.firebase.firestore.Query

class FirebaseRepository {

    inline fun <reified T> getDocument(query: Query, crossinline callback: (T?) -> Unit) {
        query.get()
            .addOnSuccessListener { documents ->
                callback(documents.firstOrNull()?.toObject(T::class.java))
            }
            .addOnFailureListener {
                callback(null)
            }
    }

    inline fun <reified T> getCollection(query: Query, crossinline callback: (ArrayList<T>?) -> Unit) {
        query.get()
            .addOnSuccessListener { documents ->
                val arrayList = ArrayList<T>()

                for (document in documents) {
                    try {
                        arrayList.add(document.toObject(T::class.java))
                    } catch (e: Exception) {
                        //Catching error incase object can't be de-serialized //TODO Analytics
                    }
                }

                callback(arrayList)
            }
            .addOnFailureListener {
                //Connection failed //TODO Analytics
                callback(null)
            }
    }

}