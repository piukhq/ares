package com.bink.wallet.scenes.pll

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.bink.wallet.data.PaymentCardDao
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.payment_card.PaymentCard
import com.bink.wallet.network.ApiService
import com.bink.wallet.scenes.loyalty_wallet.LoyaltyWalletRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody

class PllRepository(private val apiService: ApiService, private val paymentCardDao: PaymentCardDao) {

    suspend fun getPaymentCards(paymentCards: MutableLiveData<List<PaymentCard>>) {
        CoroutineScope(Dispatchers.IO).launch {
            val request = apiService.getPaymentCardsAsync()
            withContext(Dispatchers.Main) {
                try {
                    val response = request.await()
                    paymentCardDao.deleteAll()
                    paymentCardDao.storeAll(response)
                    paymentCards.value = response
                } catch (e: Throwable) {
                    Log.e(LoyaltyWalletRepository::class.simpleName, e.toString())
                }
            }
        }
    }

    suspend fun getLocalPaymentCards(localPaymentCards: MutableLiveData<List<PaymentCard>>) {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                try {
                    localPaymentCards.value = paymentCardDao.getAllAsync()
                } catch (e: Throwable) {
                    Log.e(LoyaltyWalletRepository::class.simpleName, e.toString())
                }
            }
        }
    }

    suspend fun linkPaymentCard(
        membershipCardId: String,
        paymentCardId: String,
        paymentCard: MutableLiveData<PaymentCard>, linkError: MutableLiveData<Throwable>
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val request = apiService.linkToPaymentCardAsync(membershipCardId, paymentCardId)
            withContext(Dispatchers.Main) {
                try {
                    val response = request.await()
                    paymentCard.value = response
                } catch (e: Throwable) {
                    linkError.value = e
                    Log.e(LoyaltyWalletRepository::class.simpleName, e.toString())
                }
            }
        }
    }

    suspend fun unlinkPaymentCard(paymentCardId: String, membershipCardId: String, unlinkError: MutableLiveData<Throwable>, unlinkedBody: MutableLiveData<ResponseBody>) {
        CoroutineScope(Dispatchers.IO).launch {
            val request = apiService.unlinkFromPaymentCardAsync(paymentCardId, membershipCardId)
            withContext(Dispatchers.Main) {
                try {
                    val response = request.await()
                    unlinkedBody.value = response
                } catch (e: Throwable) {
                    unlinkError.value = e
                    Log.e(LoyaltyWalletRepository::class.simpleName, e.toString())
                }
            }
        }
    }
}