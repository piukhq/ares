package com.bink.wallet.utils

const val MINUTES = "minutes"
const val HOURS = "hours"
const val DAYS = "days"
const val WEEKS = "weeks"
const val MONTHS = "months"
const val YEARS = "years"

const val NUMBER_MINUTES_IN_HOUR = 60
const val NUMBER_SECONDS_IN_MINUTE = 60
const val NUMBER_HOURS_IN_DAY = 24
const val NUMBER_DAYS_IN_WEEK = 7
const val NUMBER_WEEKS_IN_MONTH = 5
const val NUMBER_MONTHS_IN_YEAR = 12

const val ONE_THOUSAND = 1000

val ENVIRONMENTS_TO_DEBUG = listOf("debug", "beta", "mr", "nightly")

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
const val INT_ZERO = 0
const val INT_ONE_HUNDRED = 100
const val LONG_ZERO = 0L

const val PREFERENCE_MARKETING_SLUG = "marketing-bink"

const val SESSION_HANDLER_NAVIGATION_KEY = "SESSION_HANDLER_NAVIGATION_KEY"
const val SESSION_HANDLER_DESTINATION_ONBOARDING = "SESSION_HANDLER_DESTINATION_ONBOARDING"

const val CERT_PINNING_ERROR = "SSLPeerUnverifiedException"

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

const val BARCODE = "barcode"

const val ADD_AUTH_BARCODE = "ADD_AUTH_BARCODE"

const val CAMERA_REQUEST_CODE = 101
