package com.bink.wallet.data

import android.content.Context
import android.content.SharedPreferences
import com.bink.wallet.utils.enums.ApiVersion

object SharedPreferenceManager {

    private const val FILE_NAME = "BinkPrefs"
    private const val ENV_FILE_NAME = "EnvBinkPrefs"
    private const val MODE = Context.MODE_PRIVATE
    private lateinit var preferences: SharedPreferences
    private lateinit var environmentPreferences: SharedPreferences

    //----- KEYS -----
    private const val IS_ADD_JOURNEY_KEY = "isAddJourney"
    private const val IS_LOYALTY_WALLET = "isLoyaltyWalletActive"
    private const val IS_PAYMENT_EMPTY_KEY = "isPaymentEmpty"
    private const val IS_PAYMENT_JOIN_KEY = "isPaymentJoinBannerDismissed"
    private const val MEMBERSHIP_PLAN_LAST_REQUEST_TIME = "membershipPlanLastRequestTime"
    private const val IS_USER_LOGGED_IN_KEY = "isUserLoggedIn"
    private const val API_VERSION = "apiVersion"
    private const val PAYMENT_CARDS_LAST_REQUEST_TIME = "paymentCardsLastRequestTime"
    private const val MEMBERSHIP_CARDS_LAST_REQUEST_TIME = "membershipCardsLastRequestTime"

    //----- PAIRS ----
    private val IS_ADD_JOURNEY = Pair(IS_ADD_JOURNEY_KEY, false)
    private val IS_LOYALTY_SELECTED = Pair(IS_LOYALTY_WALLET, true)
    private val IS_PAYMENT_EMPTY = Pair(IS_PAYMENT_EMPTY_KEY, false)
    private val IS_PAYMENT_JOIN_HIDDEN = Pair(IS_PAYMENT_JOIN_KEY, false)
    private val IS_USER_LOGGED_IN = Pair(IS_USER_LOGGED_IN_KEY, false)

    fun init(context: Context) {
        preferences = context.getSharedPreferences(FILE_NAME, MODE)
        environmentPreferences = context.getSharedPreferences(ENV_FILE_NAME, MODE)
    }

    var storedApiUrl: String?
        get() = environmentPreferences.getString(API_VERSION, null)
        set(value) = environmentPreferences.edit {
            it.putString(API_VERSION, value)
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

    var paymentCardsLastRequestTime: Long
        get() = preferences.getLong(PAYMENT_CARDS_LAST_REQUEST_TIME, 0)
        set(value) = preferences.edit {
            it.putLong(PAYMENT_CARDS_LAST_REQUEST_TIME, value)
        }

    var membershipCardsLastRequestTime: Long
        get() = preferences.getLong(MEMBERSHIP_CARDS_LAST_REQUEST_TIME, 0)
        set(value) = preferences.edit {
            it.putLong(MEMBERSHIP_CARDS_LAST_REQUEST_TIME, value)
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