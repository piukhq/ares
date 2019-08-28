package com.bink.wallet.scenes.loyalty_details

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.bink.wallet.data.MembershipCardDao
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
}