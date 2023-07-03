package com.bink.wallet.utils.firebase

import android.util.Log
import com.google.firebase.firestore.CollectionReference
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
                        callback(null)
                        //Catching error incase object can't be de-serialized //TODO Analytics
                    }
                }

                callback(arrayList)
            }
            .addOnFailureListener {
                callback(null)
            }
    }

    fun <T> setDocument(id: String, document: T, collection: CollectionReference, callback: (Boolean) -> Unit? = {}) {
        if (document == null) {
            callback(false)
        } else {
            collection.document(id).set(document)
                .addOnSuccessListener {
                    callback(true)
                }
                .addOnFailureListener {

                    Log.d("pollStuff", "error ${it.localizedMessage}")
                    callback(false)
                }
        }
    }

    fun deleteDocument(id: String, collection: CollectionReference, callback: (Exception?) -> Unit? = {}) {
        collection.document(id).delete()
            .addOnSuccessListener {
                callback(null)
            }
            .addOnFailureListener {
                callback(it)
            }
    }

}