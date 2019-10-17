package com.bink.wallet.scenes.add_payment_card

import androidx.lifecycle.MutableLiveData
import com.bink.wallet.data.PaymentCardDao
import com.bink.wallet.model.response.payment_card.PaymentCard
import com.bink.wallet.model.response.payment_card.PaymentCardAdd
import com.bink.wallet.network.ApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PaymentCardRepository(val apiService: ApiService, val paymentCardDao: PaymentCardDao) {
    fun sendAddCard(card: PaymentCardAdd, mutableAddCard: MutableLiveData<PaymentCard>, error: MutableLiveData<Throwable>) {
        CoroutineScope(Dispatchers.IO).launch {
            val request = apiService.addPaymentCard(card)
            withContext(Dispatchers.Main) {
                try {
                    val response = request.await()
                    paymentCardDao.storeAll(listOf(response))
                    mutableAddCard.value = response
                } catch (e: Throwable) {
                    error.value = e
                }
            }
        }
    }

}