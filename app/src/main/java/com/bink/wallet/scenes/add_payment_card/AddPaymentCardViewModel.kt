package com.bink.wallet.scenes.add_payment_card

import androidx.lifecycle.MutableLiveData
import com.bink.wallet.BaseViewModel

class AddPaymentCardViewModel : BaseViewModel() {

    val cardNumber = MutableLiveData<String>()
    val expiryDate = MutableLiveData<String>()
    val cardHolder = MutableLiveData<String>()

}
