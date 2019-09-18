package com.bink.wallet.scenes.loyalty_details

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.bink.wallet.data.MembershipCardDao
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.payment_card.PaymentCard
import com.bink.wallet.network.ApiService
import com.bink.wallet.scenes.loyalty_wallet.LoyaltyWalletRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoyaltyCardDetailsRepository(private val apiService: ApiService, private val membershipCardDao: MembershipCardDao ) {

    suspend fun deleteMembershipCard(id: String?, mutableDeleteCard: MutableLiveData<String>, error: MutableLiveData<String>) {
        CoroutineScope(Dispatchers.IO).launch {
            val request = id?.let { apiService.deleteCardAsync(it) }
            withContext(Dispatchers.Main) {
                try {
                    request?.await()
                    membershipCardDao.deleteCard(id.toString())
                    mutableDeleteCard.value = id
                } catch (e: Throwable) {
                    error.value = e.toString()
                    Log.e(LoyaltyWalletRepository::class.simpleName, e.toString())
                }
            }
        }
    }

    suspend fun refreshMembershipCard(cardId: String, membershipCard: MutableLiveData<MembershipCard>) {
        CoroutineScope(Dispatchers.IO).launch {
            val request = apiService.getMembershipCardsAsync()
            withContext(Dispatchers.Main) {
                try {
                    val response = request.await()
                    membershipCard.value = response.first { card -> card.id == cardId }
                } catch (e: Throwable) {
                    Log.e(LoyaltyWalletRepository::class.simpleName, e.toString())
                }
            }
        }
    }

    suspend fun getPaymentCards(paymentCards: MutableLiveData<List<PaymentCard>>) {
        CoroutineScope(Dispatchers.IO).launch {
            val request = apiService.getPaymentCardsAsync()
            withContext(Dispatchers.Main) {
                try {
                    val response = request.await()
                    paymentCards.value = response
                } catch (e: Throwable) {
                    Log.e(LoyaltyWalletRepository::class.simpleName, e.toString())
                }
            }
        }
    }
}