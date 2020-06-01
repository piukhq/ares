package com.bink.wallet.utils.enums

import com.bink.wallet.R

enum class LinkStatus(
    val status: Float,
    val drawable: Int,
    val statusText: Int,
    val descriptionText: Int,
    var descriptionParams: List<*>? = null
) {
    STATUS_LINKED_TO_SOME_OR_ALL(
        2.1f,
        R.drawable.ic_active_linked,
        R.string.link_status_linked,
        R.string.description_linked
    ),
    STATUS_LINKABLE_NO_PAYMENT_CARDS(
        2.2f,
        R.drawable.ic_lcd_module_icons_link_error,
        R.string.link_status_linkable_no_cards,
        R.string.description_no_cards
    ),
    STATUS_LINKABLE_NO_PAYMENT_CARDS_LINKED(
        2.3f,
        R.drawable.ic_lcd_module_icons_link_error,
        R.string.link_status_linkable_no_cards,
        R.string.description_no_cards
    ),
    STATUS_LINKABLE_GENERIC_ERROR(
        2.4f,
        R.drawable.ic_lcd_module_icons_link_error,
        R.string.link_status_link_error,
        R.string.description_error
    ),
    STATUS_LINKABLE_REQUIRES_AUTH(
        2.5f,
        R.drawable.ic_lcd_module_icons_points_login,
        R.string.link_status_requires_auth,
        R.string.description_requires_auth
    ),
    STATUS_LINKABLE_REQUIRES_AUTH_PENDING(
        2.6f,
        R.drawable.ic_lcd_module_icons_points_pending,
        R.string.link_status_requires_auth_pending,
        R.string.description_requires_auth_pending
    ),
    STATUS_LINKABLE_REQUIRES_AUTH_PENDING_FAILED(
        2.7f,
        R.drawable.ic_lcd_module_icons_points_login,
        R.string.title_2_7,
        R.string.description_please_try_again
    ),
    STATUS_UNLINKABLE(
        2.8f,
        R.drawable.ic_lcd_module_icons_link_inactive,
        R.string.link_status_unlinkable,
        R.string.description_unlinkable
    ),
    STATUS_NO_REASON_CODES(
        2.9f,
        R.drawable.ic_lcd_module_icons_points_login,
        R.string.error_title,
        R.string.description_please_try_again
    ),
    STATUS_LINKABLE_REQUIRES_AUTH_GHOST_CARD(
        3.0f,
        R.drawable.ic_lcd_module_icons_points_login,
        R.string.loyalty_card_details_register,
        R.string.description_please_try_again
    ),
    STATUS_LINKABLE_SIGN_UP_FAILED(
        3.1f,
        R.drawable.ic_lcd_module_icons_points_login,
        R.string.points_sign_up_failed,
        R.string.description_please_try_again
    ),

}