package com.bink.wallet.scenes.pll

import androidx.lifecycle.MutableLiveData
import com.bink.wallet.data.PaymentCardDao
import com.bink.wallet.data.SharedPreferenceManager
import com.bink.wallet.model.response.cardlinking.CardLinkageResponse
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.payment_card.PaymentCard
import com.bink.wallet.network.ApiService
import com.bink.wallet.scenes.loyalty_wallet.LoyaltyWalletRepository
import com.bink.wallet.utils.generateUuidForPaymentCards
import com.bink.wallet.utils.generateUuidFromCardLinkageState
import com.bink.wallet.utils.logDebug
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import java.util.concurrent.LinkedBlockingQueue

class PaymentWalletRepository(
    private val apiService: ApiService,
    private val paymentCardDao: PaymentCardDao
) {
    fun getPaymentCards(
        paymentCards: MutableLiveData<List<PaymentCard>>,
        fetchError: MutableLiveData<Exception>
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val request = apiService.getPaymentCardsAsync()
            withContext(Dispatchers.Main) {
                try {
                    val response = request.await()
                    storePaymentsCards(response, fetchError)

                    SharedPreferenceManager.paymentCardsLastRequestTime = System.currentTimeMillis()

                    paymentCards.value = response.toMutableList()
                } catch (e: Exception) {
                    fetchError.value = e
                }
            }
        }
    }

    private fun storePaymentsCards(
        cards: List<PaymentCard>,
        fetchError: MutableLiveData<Exception>
    ) {
        CoroutineScope(Dispatchers.IO).launch {
           generateUuidForPaymentCards(cards, paymentCardDao)
            withContext(Dispatchers.Main) {
                try {
                    withContext(Dispatchers.IO) {
                        paymentCardDao.storeAll(cards)
                    }
                } catch (e: Exception) {
                    fetchError.value = e
                }
            }
        }
    }

    fun storePaymentCard(card: PaymentCard) {
        CoroutineScope(Dispatchers.IO).launch {
            generateUuidFromCardLinkageState(card, paymentCardDao)
            withContext(Dispatchers.Main) {
                try {
                    withContext(Dispatchers.IO) {
                        paymentCardDao.store(card)
                    }
                } catch (e: Exception) {
                    logDebug(LoyaltyWalletRepository::class.simpleName, e.toString())
                }
            }
        }
    }

    fun getLocalPaymentCards(
        localPaymentCards: MutableLiveData<List<PaymentCard>>,
        localFetchError: MutableLiveData<Exception>
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                try {
                    localPaymentCards.value = paymentCardDao.getAllAsync()
                } catch (e: Exception) {
                    localFetchError.value = e
                }
            }
        }
    }

    fun linkPaymentCard(
        membershipCardId: String,
        paymentCardId: String,
        linkError: MutableLiveData<Exception>,
        paymentCardMutableValue: MutableLiveData<PaymentCard> = MutableLiveData()
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val request = apiService.linkToPaymentCardAsync(membershipCardId, paymentCardId)
            withContext(Dispatchers.Main) {
                try {
//                    val response = request.await()
                    paymentCardMutableValue.value = request
                } catch (e: Exception) {
                    linkError.value = e
                }
            }
        }
    }

    fun unlinkPaymentCard(
        paymentCardId: String,
        membershipCardId: String,
        unlinkError: MutableLiveData<Exception>?,
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
                } catch (e: Exception) {
                    unlinkError?.value = e
                }
            }
        }
    }

    fun unlinkPaymentCards(
        paymentCards: List<PaymentCard>,
        membershipCard: MembershipCard,
        unlinkSuccesses: MutableLiveData<ArrayList<Any>>,
        unlinkErrors: MutableLiveData<ArrayList<Exception>>
    ) {
        val jobs = LinkedBlockingQueue<Deferred<*>>()
        val localSuccesses = ArrayList<Any>()
        val localErrors = ArrayList<Exception>()
        paymentCards.forEach { card ->
            CoroutineScope(Dispatchers.IO).launch {
                val result = async { apiService.unlinkFromPaymentCardAsync(card.id.toString(), membershipCard.id) }
                withContext(Dispatchers.Main) {

//                    runBlocking {
//                        for (it in jobs) {
                            try {
                                val response = result.await()
                                response.let {
                                    localSuccesses.add(response)
                                    unlinkSuccesses.value = localSuccesses

                                }
                            } catch (e: Exception) {
                                localErrors.add(e)
                                unlinkErrors.value = localErrors

                            }
//                        }
//                    }

                }
            }
        }
//        unlinkSuccesses.value = localSuccesses
//        unlinkErrors.value = localErrors
    }

    fun linkPaymentCards(
        paymentCards: List<PaymentCard>,
        membershipCard: MembershipCard,
        linkSuccesses: MutableLiveData<ArrayList<Any>>,
        linkErrors: MutableLiveData<MutableList<Exception>>
    ) {
        val jobs = LinkedBlockingQueue<Deferred<*>>()
        paymentCards.forEach { card ->
            CoroutineScope(Dispatchers.IO).launch {
                val request = async { apiService.linkToPaymentCardAsync(membershipCard.id, card.id.toString()) }
                withContext(Dispatchers.Main) {
                    val localSuccesses = ArrayList<Any>()
                    val localErrors = ArrayList<Exception>()
//                    runBlocking {
//                        for (it in jobs) {
                            try {
                                val response = request.await()
                                response.let {
//                                    localSuccesses.add(response)
                                    linkSuccesses.value = localSuccesses

                                    logEvent(card,membershipCard)

                                }
                            } catch (e: Exception) {
//                                localErrors.add(e)
                                linkErrors.value = localErrors

                            }
//                        }
//                    }
                }
            }
        }
    }


    fun deletePaymentCard(
        id: String?,
        mutableDeleteCard: MutableLiveData<ResponseBody>,
        deleteError: MutableLiveData<Exception>
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val request = id?.let { apiService.deletePaymentCardAsync(it) }
            withContext(Dispatchers.Main) {
                try {
                    val response = request?.await()
                    paymentCardDao.deletePaymentCardById(id.toString())
                    mutableDeleteCard.value = response
                } catch (e: Exception) {
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
                } catch (e: Exception) {
                    // TODO: Have error catching here in a mutable
                    logDebug(PaymentWalletRepository::class.simpleName, e.toString())
                }
            }
        }
    }

    fun getLocalData(
        localPaymentCards: MutableLiveData<List<PaymentCard>>,
        localFetchError: MutableLiveData<Exception>,
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
    private fun logEvent(
        response: Any,
        membershipCard: MembershipCard
    ) {

    }
}