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
        R.drawable.ic_active,
        R.string.view_history
    ),
    STATUS_LOGGED_IN_HISTORY_UNAVAILABLE(1.2, R.drawable.ic_active),
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
        R.string.description_see_history,
        R.string.points_login
    )
}


