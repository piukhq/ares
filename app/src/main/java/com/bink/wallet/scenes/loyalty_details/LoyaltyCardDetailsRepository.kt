package com.bink.wallet.scenes.loyalty_details

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.bink.wallet.data.MembershipCardDao
import com.bink.wallet.data.PaymentCardDao
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.payment_card.PaymentCard
import com.bink.wallet.network.ApiService
import com.bink.wallet.scenes.loyalty_wallet.LoyaltyWalletRepository
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
        error: MutableLiveData<Throwable>
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
                } catch (e: Throwable) {
                    error.value = e
                }
            }
        }
    }

    fun refreshMembershipCard(
        cardId: String,
        membershipCard: MutableLiveData<MembershipCard>,
        refreshError: MutableLiveData<Throwable>,
        addError: Boolean
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val request = apiService.getMembershipCardsAsync()
            withContext(Dispatchers.Main) {
                try {
                    val response = request.await()
                    membershipCard.value = response.first { card -> card.id == cardId }
                } catch (e: Throwable) {
                    if (addError)
                        refreshError.value = e
                }
            }
        }
    }

    fun getPaymentCards(paymentCards: MutableLiveData<List<PaymentCard>>, localStoreError: MutableLiveData<Throwable>, fetchError: MutableLiveData<Throwable>) {
        CoroutineScope(Dispatchers.IO).launch {
            val request = apiService.getPaymentCardsAsync()
            withContext(Dispatchers.Main) {
                try {
                    val response = request.await()
                    paymentCards.value = response
                    storePaymentsCards(response, localStoreError)
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

    private fun storePaymentsCards(
        cards: List<PaymentCard>,
        storeError: MutableLiveData<Throwable>
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                try {
                    withContext(Dispatchers.IO) {
                        paymentCardDao.deleteAll()
                        paymentCardDao.storeAll(cards)
                    }
                } catch (e: Throwable) {
                    storeError.value = e
                }
            }
        }
    }
}