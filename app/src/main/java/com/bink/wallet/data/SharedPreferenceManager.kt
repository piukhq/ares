package com.bink.wallet.data

import android.content.Context
import android.content.SharedPreferences

object SharedPreferenceManager {

    private const val FILE_NAME = "BinkPrefs"
    private const val MODE = Context.MODE_PRIVATE
    private lateinit var preferences: SharedPreferences

    //----- KEYS -----
    private const val IS_ADD_JOURNEY_KEY = "isAddJourney"

    //----- PAIRS ----
    private val IS_ADD_JOURNEY = Pair(IS_ADD_JOURNEY_KEY, false)

    fun init(context: Context) {
        preferences = context.getSharedPreferences(FILE_NAME, MODE)
    }
    private inline fun SharedPreferences.edit(operation: (SharedPreferences.Editor) -> Unit) {
        val editor = edit()
        operation(editor)
        editor.apply()
    }

    var isAddJourney: Boolean
        get() = preferences.getBoolean(IS_ADD_JOURNEY.first, IS_ADD_JOURNEY.second)

        set(value) = preferences.edit {
            it.putBoolean(IS_ADD_JOURNEY.first, value)
        }
}