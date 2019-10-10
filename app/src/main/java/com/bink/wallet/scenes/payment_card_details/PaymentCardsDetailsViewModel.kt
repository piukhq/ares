package com.bink.wallet.scenes.payment_card_details

import androidx.lifecycle.MutableLiveData
import com.bink.wallet.BaseViewModel
import com.bink.wallet.model.response.payment_card.PaymentCard

class PaymentCardsDetailsViewModel : BaseViewModel() {
    var paymentCard = MutableLiveData<PaymentCard>()
}
