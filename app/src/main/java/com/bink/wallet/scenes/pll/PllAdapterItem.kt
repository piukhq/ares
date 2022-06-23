package com.bink.wallet.scenes.pll

import com.bink.wallet.R
import com.bink.wallet.model.response.payment_card.PaymentCard

sealed class PllAdapterItem(val id: Int) {
    data class PaymentCardItem(
        var paymentCard: PaymentCard,
        var isSelected: Boolean = false
    ) : PllAdapterItem(R.layout.pll_payment_card_item)

}