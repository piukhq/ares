package com.bink.wallet.scenes.pll

import androidx.lifecycle.MutableLiveData
import com.bink.wallet.data.PaymentCardDao
import com.bink.wallet.data.SharedPreferenceManager
import com.bink.wallet.model.response.payment_card.PaymentCard
import com.bink.wallet.network.ApiService
import com.bink.wallet.utils.logDebug
import kotlinx.coroutines.*
import okhttp3.ResponseBody
import java.util.concurrent.LinkedBlockingQueue

class PaymentWalletRepository(
    private val apiService: ApiService,
    private val paymentCardDao: PaymentCardDao
) {
    fun getPaymentCards(
        paymentCards: MutableLiveData<List<PaymentCard>>,
        fetchError: MutableLiveData<Throwable>
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val request = apiService.getPaymentCardsAsync()
            withContext(Dispatchers.Main) {
                try {
                    val response = request.await()
                    storePaymentsCards(response, fetchError)

                    SharedPreferenceManager.paymentCardsLastRequestTime = System.currentTimeMillis()

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

    fun linkPaymentCard(
        membershipCardId: String,
        paymentCardId: String,
        linkError: MutableLiveData<Throwable>,
        paymentCardMutableValue: MutableLiveData<PaymentCard> = MutableLiveData()
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val request = apiService.linkToPaymentCardAsync(membershipCardId, paymentCardId)
            withContext(Dispatchers.Main) {
                try {
                    val response = request.await()
                    paymentCardMutableValue.value = response
                } catch (e: Throwable) {
                    linkError.value = e
                }
            }
        }
    }

    fun unlinkPaymentCard(
        paymentCardId: String,
        membershipCardId: String,
        unlinkError: MutableLiveData<Throwable>?,
        unlinkedBody: MutableLiveData<ResponseBody>?,
        paymentCard: MutableLiveData<PaymentCard>
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val request = apiService.unlinkFromPaymentCardAsync(paymentCardId, membershipCardId)
            withContext(Dispatchers.Main) {
                try {
                    val response = request.await()
                    unlinkedBody?.value = response
                    val paymentCardValue = paymentCard.value
                    paymentCardValue?.membership_cards?.forEach {
                        if (it.id == membershipCardId) {
                            it.active_link = false
                        }
                    }
                    paymentCard.value = paymentCardValue
                } catch (e: Throwable) {
                    unlinkError?.value = e
                }
            }
        }
    }

    fun unlinkPaymentCards(
        paymentCardIds: List<String>,
        membershipCardId: String,
        unlinkSuccesses: MutableLiveData<ArrayList<Any>>,
        unlinkErrors: MutableLiveData<ArrayList<Throwable>>
    ) {
        val jobs = LinkedBlockingQueue<Deferred<*>>()
        paymentCardIds.forEach { id ->
            CoroutineScope(Dispatchers.IO).launch {
                jobs.add(async { apiService.unlinkFromPaymentCardAsync(id, membershipCardId) })
                withContext(Dispatchers.Main) {
                    val localSuccesses = ArrayList<Any>()
                    val localErrors = ArrayList<Throwable>()
                    runBlocking {
                        for (it in jobs) {
                            try {
                                val response = it.await()
                                response?.let {
                                    localSuccesses.add(response)
                                }
                            } catch (e: Throwable) {
                                localErrors.add(e)
                            }
                        }
                    }
                    unlinkSuccesses.value = localSuccesses
                    unlinkErrors.value = localErrors
                }
            }
        }
    }

    fun linkPaymentCards(
        paymentCardIds: List<String>,
        membershipCardId: String,
        linkSuccesses: MutableLiveData<ArrayList<Any>>,
        linkErrors: MutableLiveData<MutableList<Throwable>>
    ) {
        val jobs = LinkedBlockingQueue<Deferred<*>>()
        paymentCardIds.forEach { id ->
            CoroutineScope(Dispatchers.IO).launch {
                jobs.add(async { apiService.linkToPaymentCardAsync(membershipCardId, id) })
                withContext(Dispatchers.Main) {
                    val localSuccesses = ArrayList<Any>()
                    val localErrors = ArrayList<Throwable>()
                    runBlocking {
                        for (it in jobs) {
                            try {
                                val response = it.await()
                                response?.let {
                                    localSuccesses.add(response)

                                }
                            } catch (e: Throwable) {
                                localErrors.add(e)
                            }
                        }
                    }
                    linkSuccesses.value = localSuccesses
                    linkErrors.value = localErrors
                }
            }
        }
    }


    fun deletePaymentCard(
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
                    logDebug(PaymentWalletRepository::class.simpleName, e.toString())
                }
            }
        }
    }

    fun getLocalData(
        localPaymentCards: MutableLiveData<List<PaymentCard>>,
        localFetchError: MutableLiveData<Throwable>,
        updateDone: MutableLiveData<Boolean>
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                try {
                    val storedPaymentCards =
                        async(Dispatchers.IO) { paymentCardDao.getAllAsync() }

                    localPaymentCards.value = storedPaymentCards.await()
                    updateDone.value = true
                } catch (exception: Exception) {
                    localFetchError.value = exception
                }
            }
        }
    }
}