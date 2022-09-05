package com.bink.wallet.utils

const val MINUTES = "minutes"
const val HOURS = "hours"
const val DAYS = "days"
const val WEEKS = "weeks"
const val MONTHS = "months"
const val YEARS = "years"

const val ONE_THOUSAND = 1000

const val EMPTY_STRING = ""
const val SPACE = " "

const val PAGE_1 = "Page1"
const val PAGE_2 = "Page2"
const val PAGE_3 = "Page3"
const val ONBOARDING_SCROLL_DURATION_SECONDS = 12000L

const val EMAIL_REGEX = "^.+@([A-Za-z0-9-]+\\.)+[A-Za-z]{2}[A-Za-z]*\$"
const val PASSWORD_REGEX = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{8,30}$"
const val JOIN_CARD = "JOIN_CARD"

const val SCROLL_DELAY = 200L

const val CONTENT_TYPE = "application/json;v=1.1"

const val FLOAT_ZERO = 0f
const val FLOAT_ONE_HUNDRED = 100f
const val INT_ONE_HUNDRED = 100

const val PREFERENCE_MARKETING_SLUG = "marketing-bink"

const val SESSION_HANDLER_NAVIGATION_KEY = "SESSION_HANDLER_NAVIGATION_KEY"
const val SESSION_HANDLER_DESTINATION_ONBOARDING = "SESSION_HANDLER_DESTINATION_ONBOARDING"

const val CERT_PINNING_GENERAL_ERROR = "SSL"

const val TWO_DECIMALS_FLOAT_FORMAT = "%.2f"
const val NO_DECIMALS_FORMAT = "%.0f"

const val RELEASE_BUILD_TYPE = "release"

const val KEYBOARD_TO_SCREEN_HEIGHT_RATIO = 0.15

val LETTER_REGEX = "[a-zA-Z]".toRegex()

const val VOUCHER_EARN_TYPE_STAMPS = "stamps"

const val DATE_FORMAT = "dd/MM/yyyy"

const val TERMS_AND_CONDITIONS_URL = "https://bink.com/terms-and-conditions/#privacy-policy"
const val PRIVACY_POLICY_URL = "https://bink.com/privacy-policy/"
const val MAGIC_LINK_URL = "https://help.bink.com/hc/en-gb/articles/4404303824786"

const val BARCODE = "barcode"
const val CARD_NUMBER = "card_number"

const val ADD_AUTH_BARCODE = "ADD_AUTH_BARCODE"

const val CAMERA_REQUEST_CODE = 101

const val PLAN_ALREADY_EXISTS = "PLAN_ALREADY_LINKED"

const val PAYMENT_CARD_STATUS_PENDING = "pending"

const val UPDATE_REQUEST_CODE = 102

const val REMOTE_CONFIG_APP_CONFIGURATION = "config_file"

const val MAGIC_LINK_LOCALE = "en_GB"
const val MAGIC_LINK_BUNDLE_ID = "com.bink.wallet"
const val MAGIC_LINK_SLUG = "matalan-reward-card"

val REMEMBERABLE_FIELD_NAMES = arrayListOf("email", "first_name", "last_name", "phone", "date of birth")
const val REMEMBER_DETAILS_KEY = "remember-my-details"
const val ALWAYS_SHOW_BARCODE_KEY = "show-barcode-always"
const val CLEAR_PREF_KEY = "clear_preferences"
const val REMEMBER_DETAILS_COMMON_NAME = "remember_my_details"
const val REMEMBER_DETAILS_DISPLAY_NAME = "Remember my details"
const val EMAIL_COMMON_NAME = "email"
const val CLEAR_CREDS_TITLE = "Clear Stored Credentials"

const val LINKING_SUPPORT_ADD = "ADD"
const val LINKING_SUPPORT_ENROL = "ENROL"

val EAN_13_BARCODE_LENGTH_LIMIT = 12..13