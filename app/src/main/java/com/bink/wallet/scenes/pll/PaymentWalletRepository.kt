package com.bink.wallet.scenes.pll

import androidx.lifecycle.MutableLiveData
import com.bink.wallet.BaseFragment
import com.bink.wallet.data.MembershipCardDao
import com.bink.wallet.data.PaymentCardDao
import com.bink.wallet.data.SharedPreferenceManager
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.payment_card.PaymentCard
import com.bink.wallet.network.ApiService
import com.bink.wallet.scenes.loyalty_wallet.wallet.LoyaltyWalletRepository
import com.bink.wallet.utils.FirebaseEvents.PLL_DELETE
import com.bink.wallet.utils.FirebaseEvents.PLL_PATCH
import com.bink.wallet.utils.FirebaseEvents.PLL_STATE_ACTIVE
import com.bink.wallet.utils.FirebaseEvents.PLL_STATE_FAILED
import com.bink.wallet.utils.FirebaseEvents.PLL_STATE_SOFT_LINK
import com.bink.wallet.utils.MembershipPlanUtils
import com.bink.wallet.utils.generateUuidForPaymentCards
import com.bink.wallet.utils.generateUuidFromCardLinkageState
import com.bink.wallet.utils.logDebug
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody

class PaymentWalletRepository(
    private val apiService: ApiService,
    private val paymentCardDao: PaymentCardDao,
    private val membershipCardDao: MembershipCardDao
) {
    fun getPaymentCards(
        paymentCards: MutableLiveData<List<PaymentCard>>,
        fetchError: MutableLiveData<Exception>
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val requestResult = withContext(Dispatchers.IO) {
                    apiService.getPaymentCardsAsync()
                }
                storePaymentsCards(requestResult, fetchError)

                SharedPreferenceManager.paymentCardsLastRequestTime = System.currentTimeMillis()
                SharedPreferenceManager.isPaymentEmpty = requestResult.isEmpty()
                SharedPreferenceManager.hasNoActivePaymentCards =
                    MembershipPlanUtils.hasNoActiveCards(requestResult)

                paymentCards.value = requestResult.toMutableList()
            } catch (e: Exception) {
                fetchError.value = e
            }
        }
    }

    suspend fun getPaymentCard(id: String): PaymentCard {
        return apiService.getPaymentCardAsync(id)
    }

    suspend fun getLocalPaymentCards(): List<PaymentCard> {
        return paymentCardDao.getAllAsync()
    }

    private fun storePaymentsCards(
        cards: List<PaymentCard>,
        fetchError: MutableLiveData<Exception>
    ) {
        CoroutineScope(Dispatchers.IO).launch {

            val cardIdsInDb = paymentCardDao.getAllAsync().map { card -> card.id }
            val idsFromApi = cards.map { cardsFromApi -> cardsFromApi.id }

            // List if id's which are in the database but not in the api data.
            val difference = idsFromApi.let { cardIdsInDb.minus(it.toSet()) }
            difference.let {
                if (it.isNotEmpty()) {
                    it.forEach { cardId ->
                        withContext(Dispatchers.IO) {
                            paymentCardDao.deletePaymentCardById(cardId.toString())
                        }
                    }
                }
            }
            generateUuidForPaymentCards(cards, paymentCardDao, membershipCardDao)
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
        membershipCard: MembershipCard,
        paymentCard: PaymentCard,
        linkError: MutableLiveData<Pair<Exception, String>>,
        paymentCardMutableLiveData: MutableLiveData<PaymentCard> = MutableLiveData(),
        membershipPlanId: String
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val paymentCardResponse =
                    withContext(Dispatchers.IO) {
                        apiService.linkToPaymentCardAsync(
                            membershipCard.id,
                            paymentCard.id.toString()
                        )
                    }
                paymentCardMutableLiveData.value = paymentCardResponse
                logPatchEvent(paymentCard, membershipCard, paymentCardResponse)


            } catch (e: Exception) {
                linkError.value = Pair(e, membershipPlanId)
                logPllFailure(paymentCard, membershipCard, true)
            }

        }
    }

    fun unlinkPaymentCard(
        paymentCard: PaymentCard,
        membershipCard: MembershipCard,
        unlinkError: MutableLiveData<Exception>?,
        paymentCardMutableLiveData: MutableLiveData<PaymentCard>
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                withContext(Dispatchers.IO) {
                    apiService.unlinkFromPaymentCardAsync(
                        paymentCard.id.toString(),
                        membershipCard.id
                    )
                }

                val paymentCardValue = paymentCardMutableLiveData.value
                paymentCardValue?.membership_cards?.forEach {
                    if (it.id == membershipCard.id) {
                        it.active_link = false
                    }
                }
                paymentCardMutableLiveData.value = paymentCardValue
                logDeleteEvent(paymentCard, membershipCard)

            } catch (e: Exception) {
                unlinkError?.value = e
                logPllFailure(paymentCard, membershipCard, false)
            }
        }
    }

    fun unlinkPaymentCards(
        paymentCards: List<PaymentCard>,
        membershipCard: MembershipCard,
        unlinkSuccesses: MutableLiveData<ArrayList<Any>>,
        unlinkErrors: MutableLiveData<ArrayList<Exception>>
    ) {
        val localSuccesses = ArrayList<Any>()
        val localErrors = ArrayList<Exception>()
        val handler = CoroutineExceptionHandler { _, exception ->
            logDebug("paymentWalletRepository", "Caught $exception")
        }
        paymentCards.forEach { card ->
            CoroutineScope(Dispatchers.IO).launch(handler) {
                val request = async {
                    apiService.unlinkFromPaymentCardAsync(
                        card.id.toString(),
                        membershipCard.id
                    )
                }
                withContext(Dispatchers.Main) {
                    try {
                        val requestResult = request.await()
                        requestResult.let {
                            localSuccesses.add(requestResult)
                            unlinkSuccesses.value = localSuccesses
                            logDeleteEvent(card, membershipCard)

                        }
                    } catch (e: Exception) {
                        localErrors.add(e)
                        unlinkErrors.value = localErrors
                        logPllFailure(card, membershipCard, false)

                    }

                }

            }
        }

    }

    fun linkPaymentCards(
        paymentCards: List<PaymentCard>,
        membershipCard: MembershipCard,
        linkSuccesses: MutableLiveData<ArrayList<Any>>,
        linkErrors: MutableLiveData<MutableList<Exception>>
    ) {
        val localSuccesses = ArrayList<Any>()
        val localErrors = ArrayList<Exception>()
        val handler = CoroutineExceptionHandler { _, exception ->
            logDebug("paymentWalletRepository", "Caught $exception")
        }
        paymentCards.forEach { card ->
            CoroutineScope(Dispatchers.IO).launch(handler) {
                val request = async {
                    apiService.linkToPaymentCardAsync(
                        membershipCard.id,
                        card.id.toString()
                    )
                }
                withContext(Dispatchers.Main) {
                    try {
                        val response = request.await()
                        response.let {
                            localSuccesses.add(response)
                            linkSuccesses.value = localSuccesses

                            logPatchEvent(card, membershipCard, it)

                        }
                    } catch (e: Exception) {
                        localErrors.add(e)
                        linkErrors.value = localErrors
                        logPllFailure(card, membershipCard, true)

                    }
                }
            }
        }
    }


    fun deletePaymentCard(
        id: String?,
        mutableDeleteCard: MutableLiveData<ResponseBody>,
        deleteError: MutableLiveData<Exception>
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                paymentCardDao.deletePaymentCardById(id.toString())
                val requestResult = id?.let {
                    withContext(Dispatchers.IO) {
                        apiService.deletePaymentCardAsync(it)
                    }
                }
                mutableDeleteCard.value = requestResult
            } catch (e: Exception) {
                deleteError.value = e
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

    private fun logPatchEvent(
        paymentCard: PaymentCard,
        membershipCard: MembershipCard,
        paymentCardResponse: PaymentCard
    ) {
        val paymentCardUuid = paymentCard.uuid
        val membershipCardUuid = membershipCard.uuid
        if (membershipCardUuid == null || paymentCardUuid == null) {
            BaseFragment.logFailedEvent(PLL_PATCH)
        } else {
            val state =
                if (paymentCardResponse.membership_cards.isNotEmpty()) PLL_STATE_ACTIVE else PLL_STATE_SOFT_LINK

            BaseFragment.logPllEvent(
                PLL_PATCH,
                BaseFragment.getPllPatchMap(paymentCardUuid, membershipCardUuid, state)
            )

        }
    }

    private fun logDeleteEvent(paymentCard: PaymentCard, membershipCard: MembershipCard) {
        val paymentCardUuid = paymentCard.uuid
        val membershipCardUuid = membershipCard.uuid
        if (membershipCardUuid == null || paymentCardUuid == null) {
            BaseFragment.logFailedEvent(PLL_DELETE)
        } else {
            BaseFragment.logPllEvent(
                PLL_DELETE,
                BaseFragment.getPllDeleteMap(paymentCardUuid, membershipCardUuid)
            )
        }

    }

    //This will be called whenever we get an error while trying to link or unlink
    private fun logPllFailure(
        paymentCard: PaymentCard,
        membershipCard: MembershipCard,
        isPatch: Boolean
    ) {

        val eventName = if (isPatch) PLL_PATCH else PLL_DELETE
        paymentCard.uuid?.let { paymentCardUuid ->
            membershipCard.uuid?.let { membershipUuid ->
                BaseFragment.logPllEvent(
                    eventName, BaseFragment.getPllPatchMap(
                        paymentCardUuid,
                        membershipUuid, PLL_STATE_FAILED
                    )
                )
            }
        }
    }

}