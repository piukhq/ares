package com.bink.wallet.data

import android.content.Context
import android.content.SharedPreferences

object SharedPreferenceManager {

    private const val FILE_NAME = "BinkPrefs"
    private const val MODE = Context.MODE_PRIVATE
    private lateinit var preferences: SharedPreferences

    //----- KEYS -----
    private const val IS_ADD_JOURNEY_KEY = "isAddJourney"
    private const val IS_LOYALTY_WALLET = "isLoyaltyWalletActive"
    private const val IS_FIRST_ONBOARDING_SCREEN = "isFirstOnboardingScreen"

    //----- PAIRS ----
    private val IS_ADD_JOURNEY = Pair(IS_ADD_JOURNEY_KEY, false)
    private val IS_LOYALTY_SELECTED = Pair(IS_LOYALTY_WALLET, true)
    private val IS_FIRST_ONBGOARDING_SCREEN = Pair(IS_FIRST_ONBOARDING_SCREEN, true)

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

    var isLoyaltySelected: Boolean
        get() = preferences.getBoolean(IS_LOYALTY_SELECTED.first, IS_LOYALTY_SELECTED.second)
        set(value) = preferences.edit {
            it.putBoolean(IS_LOYALTY_SELECTED.first, value)
        }
    var isFirstOnboardingScreen: Boolean
        get() = preferences.getBoolean(IS_FIRST_ONBGOARDING_SCREEN.first, IS_FIRST_ONBGOARDING_SCREEN.second)
        set(value) = preferences.edit {
            it.putBoolean(IS_FIRST_ONBGOARDING_SCREEN.first, value)
        }

}