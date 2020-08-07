package com.bink.wallet.utils

import java.util.Locale

object FirebaseEvents {
    const val ANALYTICS_IDENTIFIER = "identifier"
    const val ANALYTICS_CALL_TO_ACTION_TYPE = "call_to_action_pressed"

    const val ONBOARDING_VIEW = "Onboarding"
    const val LOGIN_VIEW = "Login"
    const val REGISTER_VIEW = "Register"
    const val TERMS_AND_CONDITIONS_VIEW = "SocialTermsAndConditions"
    const val LOYALTY_WALLET_VIEW = "LoyaltyWallet"
    const val PAYMENT_WALLET_VIEW = "PaymentWallet"
    const val LOYALTY_DETAIL_VIEW = "LoyaltyDetail"
    const val PAYMENT_DETAIL_VIEW = "PaymentDetail"
    const val ADD_OPTIONS_VIEW = "AddOptions"
    const val BROWSE_BRANDS_VIEW = "BrowseBrands"
    const val STORE_LINK_VIEW = "StoreViewLink"
    const val ADD_PAYMENT_CARD_VIEW = "AddPaymentCard"
    const val ADD_AUTH_FORM_VIEW = "AddAuthForm"
    const val ENROL_FORM_VIEW = "EnrolForm"
    const val REGISTRATION_FORM_VIEW = "RegistrationForm"
    const val PLL_VIEW = "PLL"
    const val INFORMATION_MODAL_VIEW = "InformationModal"
    const val SETTINGS_VIEW = "Settings"
    const val PREFERENCES_VIEW = "Preferences"
    const val SPLASH_VIEW = "SplashScreen"
    const val ADD_LOYALTY_CARD_VIEW = "AddLoyaltyCard"
    const val PAYMENT_CARD_SCAN = "payment_scan"
    const val PAYMENT_CARD_SCAN_SUCCESS = "success"
<<<<<<< HEAD
    const val ADD_PAYMENT_CARD_REQUEST = "add_payment_card_request"
    const val ADD_PAYMENT_CARD_RESPONSE_SUCCESS = "add_payment_card_response_success"
    const val ADD_PAYMENT_CARD_RESPONSE_FAILURE = "add_payment_card_response_failure"
    const val FIREBASE_PAYMENT_SCHEME_KEY = "payment_scheme"
    const val FIREBASE_CLIENT_ACCOUNT_ID = "client_account_id"
    const val ADD_PAYMENT_CARD_CLIENT_ACCOUNT_ID_KEY = "client_account_id"
    const val ADD_PAYMENT_CARD_ACCOUNT_IS_NEW_KEY = "account_is_new"
    const val ADD_PAYMENT_CARD_PAYMENT_STATUS_NEW_KEY = "payment_status"
    const val DELETE_PAYMENT_CARD_REQUEST = "delete_payment_card"
    const val DELETE_PAYMENT_CARD_RESPONSE_SUCCESS = "delete_payment_card_response_success"
    const val DELETE_PAYMENT_CARD_RESPONSE_FAILURE = "delete_payment_card_response_fail"
    const val FIREBASE_TRUE = "true"
    const val FIREBASE_FALSE = "false"

=======
    const val ONBOARDING_START = "onboarding_start"
    const val ONBOARDING_USER_COMPLETE = "onboarding_user_complete"
    const val ONBOARDING_SERVICE_COMPLETE = "onboarding_service_complete"
    const val ONBOARDING_END = "onboarding_end"
    const val ONBOARDING_JOURNEY_FACEBOOK= "FACEBOOK"
    const val ONBOARDING_JOURNEY_REGISTER = "REGISTER"
    const val ONBOARDING_JOURNEY_LOGIN = "LOGIN"
    const val ONBOARDING_JOURNEY_KEY = "onboarding_journey"
    const val ONBOARDING_ID_KEY = "onboarding_id"
    const val ONBOARDING_SUCCESS_KEY = "onboarding_success"
    const val ONBOARDING_SUCESS_TRUE = "true"
    const val ONBOARDING_SUCESS_FALSE = "false"
>>>>>>> feature/AB20-818_onboarding_analytics

    fun getFirebaseIdentifier(view: String, buttonTitle: String): String {
        return view + "." + stringToCamelcase(buttonTitle)
    }

    private fun stringToCamelcase(string: String): String {
        val words: Array<String> = string.split(" ").toTypedArray()
        val sb = StringBuilder()
        if (words[0].isNotEmpty()) {
            sb.append(
                Character.toUpperCase(words[0][0]).toString() + words[0].subSequence(
                    1,
                    words[0].length
                ).toString().toLowerCase(Locale.ENGLISH)
            )
            for (i in 1 until words.size) {
                sb.append(
                    Character.toUpperCase(words[i][0]).toString() + words[i].subSequence(
                        1,
                        words[i].length
                    ).toString().toLowerCase(Locale.ENGLISH)
                )
            }
        }
        return sb.toString()
    }
}