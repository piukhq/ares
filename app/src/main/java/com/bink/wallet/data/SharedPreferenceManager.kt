package com.bink.wallet.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

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
    private const val BACKEND_VERSION = "backendVersion"
    private const val PAYMENT_CARDS_LAST_REQUEST_TIME = "paymentCardsLastRequestTime"
    private const val MEMBERSHIP_CARDS_LAST_REQUEST_TIME = "membershipCardsLastRequestTime"
    private const val ZENDESK_REQUEST_UPDATE = "updateAvailable"
    private const val CONTACT_US_CLICKED = "contactUsClicked"
    private const val SCANNED_LOYALTY_BARCODE = "scannedLoyaltyBarcode"
    private const val DID_ATTEMPT_TO_ADD_PAYMENT_CARD = "didAttemptToAddPaymentCard"
    private const val BARCODE = "barcode"
    private const val CARD_NUMBER = "cardNumber"
    private const val ADD_PAYMENT_CARD_REQUEST_UUID = "add_payment_card_request_uuid"
    private const val ADD_PAYMENT_CARD_SUCCESS_HTTP_CODE = "add_payment_card_success_http_code"
    private const val FIREBASE_UUID = "firebase_uuid"
    private const val ADD_LOYALTY_CARD_REQUEST_UUID = "add_loyalty_card_request_uuid"
    private const val ADD_LOYALTY_CARD_SUCCESS_HTTP_CODE = "add_loyalty_card_success_http_code"
    private const val ADD_LOYALTY_CARD_JOURNEY_TYPE = "add_loyalty_card_journey_type"
    private const val IS_SCANNED_CARD = "is_scanned_card"
    private const val BARCODE_VALUE = "barcode_value"
    private const val CARD_NUMBER_VALUE = "cardNumber_value"
    private const val HAS_NO_ACTIVE_PAYMENT_CARD = "has_no_active_payment_cards"
    private const val LAST_REVIEW_MINOR = "last_review_minor"
    private const val FIRST_OPEN_DATE = "first_open_date"
    private const val TOTAL_OPEN_COUNT = "total_open_count"
    private const val ADDED_NEW_PLL = "added_new_pll"
    private const val LAST_SEEN_TRANSACTIONS = "last_seen_transactions"
    private const val HAS_NEW_TRANSACTIONS = "has_new_transactions"
    private const val LOYALTY_WALLET_ORDER = "loyalty_wallet_order"
    private const val PAYMENT_WALLET_ORDER = "payment_wallet_order"

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

    var storedBackendVersion: String?
        get() = environmentPreferences.getString(BACKEND_VERSION, null)
        set(value) = environmentPreferences.edit {
            it.putString(BACKEND_VERSION, value)
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

    var hasNoActivePaymentCards: Boolean
        get() = preferences.getBoolean(HAS_NO_ACTIVE_PAYMENT_CARD, false)
        set(value) = preferences.edit {
            it.putBoolean(HAS_NO_ACTIVE_PAYMENT_CARD, value)
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

    var isResponseAvailable: Boolean
        get() = preferences.getBoolean(ZENDESK_REQUEST_UPDATE, false)
        set(value) = preferences.edit {
            it.putBoolean(ZENDESK_REQUEST_UPDATE, value)
        }

    var hasContactUsBeenClicked: Boolean
        get() = preferences.getBoolean(CONTACT_US_CLICKED, false)
        set(value) = preferences.edit {
            it.putBoolean(CONTACT_US_CLICKED, value)
        }
    var didAttemptToAddPaymentCard: Boolean
        get() = preferences.getBoolean(DID_ATTEMPT_TO_ADD_PAYMENT_CARD, false)
        set(value) = preferences.edit {
            it.putBoolean(DID_ATTEMPT_TO_ADD_PAYMENT_CARD, value)
        }

    var scannedLoyaltyBarCode: String?
        get() = preferences.getString(SCANNED_LOYALTY_BARCODE, null)
        set(value) = preferences.edit {
            it.putString(SCANNED_LOYALTY_BARCODE, value)
        }
    var isNowBarcode: Boolean
        get() = preferences.getBoolean(BARCODE, false)
        set(value) = preferences.edit {
            it.putBoolean(BARCODE, value)
        }
    var isNowCardNumber: Boolean
        get() = preferences.getBoolean(CARD_NUMBER, false)
        set(value) = preferences.edit {
            it.putBoolean(CARD_NUMBER, value)
        }

    var addPaymentCardSuccessHttpCode: Int
        get() = preferences.getInt(ADD_PAYMENT_CARD_SUCCESS_HTTP_CODE, 0)
        set(value) = preferences.edit {
            it.putInt(ADD_PAYMENT_CARD_SUCCESS_HTTP_CODE, value)
        }


    var firebaseEventUuid: String?
        get() = preferences.getString(FIREBASE_UUID, null)
        set(value) = preferences.edit {
            it.putString(FIREBASE_UUID, value)
        }

    var addLoyaltyCardSuccessHttpCode: Int
        get() = preferences.getInt(ADD_LOYALTY_CARD_SUCCESS_HTTP_CODE, 0)
        set(value) = preferences.edit {
            it.putInt(ADD_LOYALTY_CARD_SUCCESS_HTTP_CODE, value)
        }
    var addLoyaltyCardJourneyType: String?
        get() = preferences.getString(ADD_LOYALTY_CARD_JOURNEY_TYPE, null)
        set(value) = preferences.edit {
            it.putString(ADD_LOYALTY_CARD_JOURNEY_TYPE, value)
        }
    var isScannedCard: Boolean
        get() = preferences.getBoolean(IS_SCANNED_CARD, false)
        set(value) = preferences.edit {
            it.putBoolean(IS_SCANNED_CARD, value)
        }
    var barcodeValue: String?
        get() = preferences.getString(BARCODE_VALUE, null)
        set(value) = preferences.edit {
            it.putString(BARCODE_VALUE, value)
        }
    var cardNumberValue: String?
        get() = preferences.getString(CARD_NUMBER_VALUE, null)
        set(value) = preferences.edit {
            it.putString(CARD_NUMBER_VALUE, value)
        }

    var lastReviewedMinor: String?
        get() = preferences.getString(LAST_REVIEW_MINOR, null)
        set(value) = preferences.edit {
            it.putString(LAST_REVIEW_MINOR, value)
        }

    var firstOpenDate: String?
        get() = preferences.getString(FIRST_OPEN_DATE, null)
        set(value) = preferences.edit {
            it.putString(FIRST_OPEN_DATE, value)
        }

    var totalOpenCount: Int
        get() = preferences.getInt(TOTAL_OPEN_COUNT, 0)
        set(value) = preferences.edit {
            it.putInt(TOTAL_OPEN_COUNT, value)
        }

    var hasAddedNewPll: Boolean
        get() = preferences.getBoolean(ADDED_NEW_PLL, false)
        set(value) = preferences.edit {
            it.putBoolean(ADDED_NEW_PLL, value)
        }

    var lastSeenTransactions: String?
        get() = preferences.getString(LAST_SEEN_TRANSACTIONS, null)
        set(value) = preferences.edit {
            it.putString(LAST_SEEN_TRANSACTIONS, value)
        }

    var hasNewTransactions: Boolean
        get() = preferences.getBoolean(HAS_NEW_TRANSACTIONS, false)
        set(value) = preferences.edit {
            it.putBoolean(HAS_NEW_TRANSACTIONS, value)
        }

    var loyaltyWalletOrder: String?
        get() = preferences.getString(LOYALTY_WALLET_ORDER, null)
        set(value) = preferences.edit {
            it.putString(LOYALTY_WALLET_ORDER, value)
        }

    var paymentWalletOrder: String?
        get() = preferences.getString(PAYMENT_WALLET_ORDER, null)
        set(value) = preferences.edit {
            it.putString(PAYMENT_WALLET_ORDER, value)
        }


    fun clear() {
        /**
         * Saving the current wallet orders and re-writing them to the shared preferences as its the only data that needs to persist over logouts.
         */
        val oldLoyaltyWalletOrder = loyaltyWalletOrder
        val oldPaymentWalletOrder = paymentWalletOrder

        val editor = preferences.edit()
        editor.clear()
        editor.apply()

        loyaltyWalletOrder = oldLoyaltyWalletOrder
        paymentWalletOrder = oldPaymentWalletOrder
    }
}