package com.bink.wallet.utils.enums

import com.bink.wallet.R

enum class PLLCardStatus(val status: String, val display: Int, val linkImage: Int) {
    NONE("", R.string.empty_string, 0),
    LINKED("linked", R.string.loyalty_card_pll_linked, R.drawable.ic_linked),
    LINK_NOW("link_now", R.string.loyalty_card_pll_link_now, R.drawable.ic_unlinked),
    RETRY("retry", R.string.loyalty_card_pll_retry, 0),
    PENDING("pending", R.string.loyalty_card_pll_pending, 0)
}