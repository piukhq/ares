package com.bink.wallet.model.payment_card

import com.bink.wallet.model.response.payment_card.PaymentCard
import com.bink.wallet.model.response.payment_card.PaymentMembershipCard

object RebuildPaymentCard {
    fun rebuild(originalCard: PaymentCard, newCards: List<PaymentMembershipCard>): PaymentCard {
        val newCard: PaymentCard
        with (originalCard) {
            newCard = PaymentCard(
                id,
                newCards,
                status,
                card,
                images,
                account
            )
        }
        return newCard
    }
}