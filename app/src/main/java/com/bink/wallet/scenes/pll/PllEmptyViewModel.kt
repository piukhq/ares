package com.bink.wallet.scenes.pll

import androidx.databinding.ObservableBoolean
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bink.wallet.BaseViewModel
import com.bink.wallet.model.response.payment_card.PaymentCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PllEmptyViewModel(val paymentWalletRepository: PaymentWalletRepository) : BaseViewModel() {

    var isLCDJourney = ObservableBoolean()

    private val _paymentCards = MutableLiveData<List<PaymentCard>>()
    val paymentCards: LiveData<List<PaymentCard>>
        get() = _paymentCards

    private val _paymentCardsError = MutableLiveData<Exception>()
    val paymentCardsError: LiveData<Exception>
        get() = _paymentCardsError

    fun getPaymentCards() {
        viewModelScope.launch {
            try {
                val paymentCards = withContext(Dispatchers.IO) { paymentWalletRepository.getLocalPaymentCards() }
                _paymentCards.value = paymentCards
            } catch (e: Exception) {
                _paymentCardsError.value = e
            }
        }
    }

}