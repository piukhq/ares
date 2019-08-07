package com.bink.wallet.scenes.loyalty_wallet

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.bink.wallet.network.ApiService
import com.bink.wallet.scenes.login.LoginRepository
import com.bink.wallet.scenes.loyalty_wallet.model.MembershipCard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class LoyaltyWalletRepository(private val apiService: ApiService) {

    private var mutableMembershipCard: MutableLiveData<List<MembershipCard>> = MutableLiveData()
    private var mutableDeleteCard: MutableLiveData<Any> = MutableLiveData()

    fun retrieveMembershipCards(): MutableLiveData<List<MembershipCard>> {
        CoroutineScope(Dispatchers.IO).launch {
            val request = apiService.getMembershipCards()
            withContext(Dispatchers.Main) {
                try {
                    val response = request.await()
                    mutableMembershipCard.value = response.toMutableList()
                } catch (e: Throwable) {
                    Log.e(LoginRepository::class.simpleName, e.toString())
                }
            }
        }
        return mutableMembershipCard
    }

    fun deleteMembershipCard(): MutableLiveData<Any> {
        CoroutineScope(Dispatchers.IO).launch {
            val request = apiService.getMembershipCards()
            withContext(Dispatchers.Main) {
                try {
                    val response = request.await()
                    mutableDeleteCard.value = response
                } catch (e: Throwable) {
                    Log.e(LoginRepository::class.simpleName, e.toString())
                }
            }
        }
        return mutableDeleteCard
    }
}