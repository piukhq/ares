package com.bink.wallet.model.response.payment_card

import com.bink.wallet.model.response.payment_card.PaymentCard

data class PllPaymentCardWrapper(
    var paymentCard: PaymentCard,
    var isSelected: Boolean = false
)