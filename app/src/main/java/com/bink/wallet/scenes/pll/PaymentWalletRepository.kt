package com.bink.wallet.scenes.pll

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.bink.wallet.data.PaymentCardDao
import com.bink.wallet.model.response.payment_card.PaymentCard
import com.bink.wallet.network.ApiService
import kotlinx.coroutines.*
import okhttp3.ResponseBody

class PaymentWalletRepository(
    private val apiService: ApiService,
    private val paymentCardDao: PaymentCardDao
) {
    suspend fun getPaymentCards(
        paymentCards: MutableLiveData<List<PaymentCard>>,
        fetchError: MutableLiveData<Throwable>
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val request = apiService.getPaymentCardsAsync()
            withContext(Dispatchers.Main) {
                try {
                    val response = request.await()
                    storePaymentsCards(response, fetchError)
                    paymentCards.value = response.toMutableList()
                } catch (e: Throwable) {
                    fetchError.value = e
                }
            }
        }
    }

    private fun storePaymentsCards(
        cards: List<PaymentCard>,
        fetchError: MutableLiveData<Throwable>
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                try {
                    withContext(Dispatchers.IO) {
                        paymentCardDao.deleteAll()
                        paymentCardDao.storeAll(cards)
                    }
                } catch (e: Throwable) {
                    fetchError.value = e
                }
            }
        }
    }

    fun getLocalPaymentCards(
        localPaymentCards: MutableLiveData<List<PaymentCard>>,
        localFetchError: MutableLiveData<Throwable>
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                try {
                    localPaymentCards.value = paymentCardDao.getAllAsync()
                } catch (e: Throwable) {
                    localFetchError.value = e
                }
            }
        }
    }

    suspend fun linkPaymentCard(
        membershipCardId: String,
        paymentCardId: String,
        paymentCard: MutableLiveData<PaymentCard>,
        linkError: MutableLiveData<Throwable>,
        paymentCardMutableValue: MutableLiveData<PaymentCard> = MutableLiveData()
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val request = apiService.linkToPaymentCardAsync(membershipCardId, paymentCardId)
            withContext(Dispatchers.Main) {
                try {
                    val response = request.await()
                    paymentCard.value = response

                    val paymentCardValue = paymentCardMutableValue.value
                    paymentCardValue?.membership_cards?.forEach {
                        if (it.id == membershipCardId) {
                            it.active_link = true
                        }
                    }

                    paymentCardMutableValue.value = paymentCardValue
                } catch (e: Throwable) {
                    linkError.value = e
                }
            }
        }
    }

    suspend fun unlinkPaymentCard(
        paymentCardId: String,
        membershipCardId: String,
        unlinkError: MutableLiveData<Throwable>,
        unlinkedBody: MutableLiveData<ResponseBody>,
        paymentCard: MutableLiveData<PaymentCard> = MutableLiveData()
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val request = apiService.unlinkFromPaymentCardAsync(paymentCardId, membershipCardId)
            withContext(Dispatchers.Main) {
                try {
                    val response = request.await()
                    unlinkedBody.value = response

                    val paymentCardValue = paymentCard.value
                    paymentCardValue?.membership_cards?.forEach {
                        if (it.id == membershipCardId) {
                            it.active_link = false
                        }
                    }

                    paymentCard.value = paymentCardValue
                } catch (e: Throwable) {
                    unlinkError.value = e
                }
            }
        }
    }

    suspend fun deletePaymentCard(
        id: String?,
        mutableDeleteCard: MutableLiveData<ResponseBody>,
        deleteError: MutableLiveData<Throwable>
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val request = id?.let { apiService.deletePaymentCardAsync(it) }
            withContext(Dispatchers.Main) {
                try {
                    val response = request?.await()
                    paymentCardDao.deletePaymentCardById(id.toString())
                    mutableDeleteCard.value = response
                } catch (e: Throwable) {
                    deleteError.value = e
                }
            }
        }
    }

    fun clearPaymentCards() {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                try {
                    paymentCardDao.deleteAll()
                } catch (e: Throwable) {
                    // TODO: Have error catching here in a mutable
                    Log.d(PaymentWalletRepository::class.simpleName, e.toString())
                }
            }
        }
    }
}