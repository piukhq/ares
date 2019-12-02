package com.bink.wallet.utils.enums

import com.bink.wallet.R

enum class FooterType(
    val type: String,
    val footerTitle: Int,
    val footerDescription: Int
) {
    ABOUT("0", R.string.about_membership, R.string.learn_more),
    SECURITY("1", R.string.security_privacy, R.string.how_we_protect),
    DELETE("2", R.string.delete_card, R.string.delete_card),
    RENAME("3", R.string.rename_card, R.string.rename_card_description),
    DELETE_PAYMENT("4",  R.string.delete_card, R.string.remove_card)
}