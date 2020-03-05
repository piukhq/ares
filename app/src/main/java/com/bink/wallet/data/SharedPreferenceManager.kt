package com.bink.wallet.data

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

object SharedPreferenceManager {

    private const val FILE_NAME = "BinkPrefs"
    private const val MODE = Context.MODE_PRIVATE
    private lateinit var preferences: SharedPreferences

    //----- KEYS -----
    private const val IS_ADD_JOURNEY_KEY = "isAddJourney"
    private const val IS_LOYALTY_WALLET = "isLoyaltyWalletActive"
    private const val IS_PAYMENT_EMPTY_KEY = "isPaymentEmpty"
    private const val IS_PAYMENT_JOIN_KEY = "isPaymentJoinBannerDismissed"
    private const val MEMBERSHIP_PLAN_LAST_REQUEST_TIME = "membershipPlanLastRequestTime"
    private const val IS_USER_LOGGED_IN_KEY = "isUserLoggedIn"

    //----- PAIRS ----
    private val IS_ADD_JOURNEY = Pair(IS_ADD_JOURNEY_KEY, false)
    private val IS_LOYALTY_SELECTED = Pair(IS_LOYALTY_WALLET, true)
    private val IS_PAYMENT_EMPTY = Pair(IS_PAYMENT_EMPTY_KEY, false)
    private val IS_PAYMENT_JOIN_HIDDEN = Pair(IS_PAYMENT_JOIN_KEY, false)
    private val IS_USER_LOGGED_IN = Pair(IS_USER_LOGGED_IN_KEY, false)

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

    var isPaymentEmpty: Boolean
        get() = preferences.getBoolean(IS_PAYMENT_EMPTY.first, IS_PAYMENT_EMPTY.second)
        set(value) = preferences.edit {
            it.putBoolean(IS_PAYMENT_EMPTY.first, value)
        }

    var isPaymentJoinBannerDismissed: Boolean
        get() = preferences.getBoolean(IS_PAYMENT_JOIN_HIDDEN.first, IS_PAYMENT_JOIN_HIDDEN.second)
        set(value) = preferences.edit {
            it.putBoolean(IS_PAYMENT_JOIN_HIDDEN.first, value)
        }

    var membershipPlansLastRequestTime: Long
        get() = preferences.getLong(MEMBERSHIP_PLAN_LAST_REQUEST_TIME, 0)
        set(value) = preferences.edit {
            it.putLong(MEMBERSHIP_PLAN_LAST_REQUEST_TIME, value)
        }

    var isUserLoggedIn: Boolean
        get() = preferences.getBoolean(IS_USER_LOGGED_IN_KEY, false)
        set(value) = preferences.edit {
            it.putBoolean(IS_USER_LOGGED_IN_KEY, value)
        }

    fun clear() {
        val editor = preferences.edit()
        editor.clear()
        editor.apply()
    }
}