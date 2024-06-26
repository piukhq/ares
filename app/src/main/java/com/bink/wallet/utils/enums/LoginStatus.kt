package com.bink.wallet.utils.enums

import com.bink.wallet.R

enum class LoginStatus(
    val status: Double,
    val pointsImage: Int,
    val pointsDescription: Int? = null,
    val pointsText: Int? = null
) {
    STATUS_LOGGED_IN_HISTORY_AVAILABLE(
        1.1,
        R.drawable.ic_active_points,
        R.string.view_history
    ),
    STATUS_LOGGED_IN_HISTORY_UNAVAILABLE(1.2, R.drawable.ic_active_points),
    STATUS_NOT_LOGGED_IN_HISTORY_UNAVAILABLE(
        1.3,
        R.drawable.ic_lcd_module_icons_points_login,
        R.string.description_see_history,
        R.string.points_login
    ),
    STATUS_NOT_LOGGED_IN_HISTORY_AVAILABLE(
        1.4,
        R.drawable.ic_lcd_module_icons_points_login,
        R.string.description_see_history,
        R.string.points_login
    ),
    STATUS_LOGIN_UNAVAILABLE(
        1.5,
        R.drawable.ic_lcd_module_icons_points_inactive,
        R.string.description_not_available,
        R.string.history_text
    ),
    STATUS_LOGIN_FAILED(
        1.6,
        R.drawable.ic_lcd_module_icons_points_login,
        R.string.description_retry_login,
        R.string.points_retry_login
    ),
    STATUS_PENDING(
        1.7,
        R.drawable.ic_lcd_module_icons_points_pending,
        R.string.description_please_wait,
        R.string.points_pending
    ),
    STATUS_SIGN_UP_FAILED(
        1.8,
        R.drawable.ic_lcd_module_icons_points_login,
        R.string.description_please_try_again,
        R.string.points_sign_up_failed
    ),
    STATUS_CARD_ALREADY_EXISTS(
        1.12,
        R.drawable.ic_lcd_module_icons_points_login,
        R.string.points_login_description,
        R.string.points_login_account_exists
    ),
    STATUS_NO_REASON_CODES(
        1.13,
        R.drawable.ic_lcd_module_icons_points_login,
        R.string.description_please_try_again,
        R.string.error_title
    ),
    STATUS_REGISTRATION_REQUIRED_GHOST_CARD(
        1.14,
        R.drawable.ic_lcd_module_icons_points_login,
        R.string.description_see_history,
        R.string.loyalty_card_details_register
    ),
    STATUS_LOGGED_IN_HISTORY_AND_VOUCHERS_AVAILABLE(
        1.15,
        R.drawable.ic_active_points,
        R.string.points_view_history,
        R.string.points_earning
    ),
}


