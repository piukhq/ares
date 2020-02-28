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