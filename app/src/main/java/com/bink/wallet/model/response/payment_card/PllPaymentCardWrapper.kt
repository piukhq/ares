package com.bink.wallet.model.response.payment_card

data class PllPaymentCardWrapper(
    var paymentCard: PaymentCard,
    var isSelected: Boolean = false
)