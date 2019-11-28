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

const val JWT_HEADER_NAME_ALGORITHM  = "alg"
const val JWT_HEADER_VALUE_ALGORITHM = "HS512"
const val JWT_HEADER_NAME_TYPE       = "typ"
const val JWT_HEADER_VALUE_TYPE      = "JWT"

const val JWT_PAYLOAD_TYPE_ORGANISATION  = "organisation_id"
const val JWT_PAYLOAD_VALUE_ORGANISATION = "Loyalty Angels"
const val JWT_PAYLOAD_TYPE_BUNDLE        = "bundle_id"
const val JWT_PAYLOAD_VALUE_BUNDLE       = "com.bink.bink20dev"
const val JWT_PAYLOAD_TYPE_USER          = "user_id"
const val JWT_PAYLOAD_TYPE_PROPERTY      = "property_id"
const val JWT_PAYLOAD_VALUE_PROPERTY     = "not currently used for authentication"
const val JWT_PAYLOAD_TYPE_TIME          = "iat"

const val HMAC_TYPE = "HmacSHA512"

val ENVIRONMENTS_TO_DEBUG = listOf("debug", "beta", "mr", "nightly")

const val EMPTY_STRING = ""
const val SPACE = " "

const val PAGE_1 = "Page1"
const val PAGE_2 = "Page2"
const val PAGE_3 = "Page3"
const val ONBOARDING_SCROLL_DURATION_SECONDS = 12000L

const val PASSWORD_REGEX = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{8,30}$"
const val JOIN_CARD = "JOIN_CARD"
