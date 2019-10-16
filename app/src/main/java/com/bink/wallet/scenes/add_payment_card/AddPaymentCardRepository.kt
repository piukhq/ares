package com.bink.wallet.scenes.add_payment_card

import com.bink.wallet.model.response.payment_card.PaymentCardAdd
import com.bink.wallet.network.ApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddPaymentCardRepository(private val apiService: ApiService) {
    fun sendAddCard(card: PaymentCardAdd) {
        CoroutineScope(Dispatchers.IO).launch {
            val request = apiService.addPaymentCard(card)
            withContext(Dispatchers.Main) {
                try {
                    val response = request.await()
                } catch (e: Throwable) {
                }
            }
        }
    }

}