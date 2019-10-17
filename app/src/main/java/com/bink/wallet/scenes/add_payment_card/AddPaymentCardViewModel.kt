package com.bink.wallet.scenes.add_payment_card

import androidx.lifecycle.MutableLiveData
import com.bink.wallet.BaseViewModel
import com.bink.wallet.model.response.payment_card.PaymentCard
import com.bink.wallet.model.response.payment_card.PaymentCardAdd

class AddPaymentCardViewModel constructor(
    private val paymentCardRepository: PaymentCardRepository
) : BaseViewModel() {
    val paymentCard = MutableLiveData<PaymentCard>()
    val error = MutableLiveData<Throwable>()

    fun sendAddCard(card: PaymentCardAdd) {
        error.value = null
        paymentCardRepository.sendAddCard(card, paymentCard, error)
    }
}
