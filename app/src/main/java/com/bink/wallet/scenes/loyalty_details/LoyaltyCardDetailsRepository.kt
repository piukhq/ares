package com.bink.wallet.scenes.loyalty_details

import androidx.lifecycle.MutableLiveData
import com.bink.wallet.data.MembershipCardDao
import com.bink.wallet.data.PaymentCardDao
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.payment_card.PaymentCard
import com.bink.wallet.network.ApiService
import com.bink.wallet.utils.generateUuidForMembershipCardPullToRefresh
import com.bink.wallet.utils.generateUuidForPaymentCards
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class LoyaltyCardDetailsRepository(
    private val apiService: ApiService,
    private val membershipCardDao: MembershipCardDao,
    private val paymentCardDao: PaymentCardDao
) {

    suspend fun deleteMembershipCard(
        id: String?,
        mutableDeleteCard: MutableLiveData<String>,
        error: MutableLiveData<Exception>
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val request = id?.let { apiService.deleteCardAsync(it) }
            withContext(Dispatchers.Main) {
                try {
                    request?.await()
                    membershipCardDao.deleteCard(id.toString())
                    mutableDeleteCard.value = id
                } catch (e: HttpException) {
                    error.value = e
                } catch (e: Exception) {
                    error.value = e
                }
            }
        }
    }

    fun refreshMembershipCard(
        cardId: String,
        membershipCard: MutableLiveData<MembershipCard>,
        refreshError: MutableLiveData<Exception>,
        addError: Boolean
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val request = apiService.getMembershipCardsAsync()
            withContext(Dispatchers.Main) {
                try {
                    val response = request.await()
                    membershipCard.value = response.first { card -> card.id == cardId }
                        .also {
                            generateUuidForMembershipCardPullToRefresh(it,membershipCardDao)
                            membershipCardDao.storeMembershipCard(it)
                        }
                } catch (e: Exception) {
                    if (addError)
                        refreshError.value = e
                }
            }
        }
    }

    fun getPaymentCards(
        paymentCards: MutableLiveData<List<PaymentCard>>,
        localStoreError: MutableLiveData<Exception>,
        fetchError: MutableLiveData<Exception>
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val request = apiService.getPaymentCardsAsync()
            withContext(Dispatchers.Main) {
                try {
                    val response = request.await()
                    storePaymentsCards(response, localStoreError)
                    paymentCards.value = response
                } catch (e: Exception) {
                    fetchError.value = e
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

    private fun storePaymentsCards(
        cards: List<PaymentCard>,
        storeError: MutableLiveData<Exception>
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            generateUuidForPaymentCards(cards, paymentCardDao)
            withContext(Dispatchers.Main) {
                try {
                    withContext(Dispatchers.IO) {
                        paymentCardDao.storeAll(cards)
                    }
                } catch (e: Exception) {
                    storeError.value = e
                }
            }
        }
    }
}