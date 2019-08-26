package com.bink.wallet.scenes.loyalty_wallet

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.bink.wallet.data.MembershipCardDao
import com.bink.wallet.data.MembershipPlanDao
import com.bink.wallet.model.request.membership_card.MembershipCardRequest
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.network.ApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class LoyaltyWalletRepository(
    private val apiService: ApiService,
    private val membershipCardDao: MembershipCardDao,
    private val membershipPlanDao: MembershipPlanDao
) {

    fun retrieveMembershipCards(mutableMembershipCards: MutableLiveData<List<MembershipCard>>) {
        CoroutineScope(Dispatchers.IO).launch {
            val request = apiService.getMembershipCardsAsync()
            withContext(Dispatchers.Main) {
                try {
                    val response = request.await()
                    mutableMembershipCards.value = response.toMutableList()
                    membershipCardDao.storeAll(response)
                } catch (e: Throwable) {
                    Log.e(LoyaltyWalletRepository::class.simpleName, e.toString())
                }
            }
        }
    }

    fun retrieveStoredMembershipCards(localMembershipCards: MutableLiveData<List<MembershipCard>>) {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                try {
                    val response = membershipCardDao.getAllAsync()
                    localMembershipCards.value = response.toMutableList()
                } catch (e: Throwable) {
                    Log.e(LoyaltyWalletRepository::class.simpleName, e.toString())
                }
            }
        }
    }

    fun retrieveMembershipPlans(mutableMembershipPlans: MutableLiveData<List<MembershipPlan>>) {
        CoroutineScope(Dispatchers.IO).launch {
            val request = apiService.getMembershipPlansAsync()
            withContext(Dispatchers.Main) {
                try {
                    val response = request.await()
                    mutableMembershipPlans.value = response.toMutableList()
                    membershipPlanDao.storeAll(response)
                } catch (e: Throwable) {
                    Log.e(LoyaltyWalletRepository::class.simpleName, e.toString())
                }
            }
        }
    }

    fun retrieveStoredMembershipPlans(localMembershipPlans: MutableLiveData<List<MembershipPlan>>) {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                try {
                    val response = membershipPlanDao.getAllAsync()
                    localMembershipPlans.value = response.toMutableList()
                } catch (e: Throwable) {
                    Log.e(LoyaltyWalletRepository::class.simpleName, e.toString())
                }
            }
        }
    }

    fun deleteMembershipCard(id: String?, mutableDeleteCard: MutableLiveData<String>) {
        CoroutineScope(Dispatchers.IO).launch {
            val request = id?.let { apiService.deleteCardAsync(it) }
            withContext(Dispatchers.Main) {
                try {
                    request?.await()
                    mutableDeleteCard.value = id
                } catch (e: Throwable) {
                    Log.e(LoyaltyWalletRepository::class.simpleName, e.toString())
                }
            }
        }
    }

    fun createMembershipCard(
        membershipCardRequest: MembershipCardRequest,
        mutableMembershipCard: MutableLiveData<MembershipCard>
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val request = apiService.createMembershipCardAsync(membershipCardRequest)
            withContext(Dispatchers.Main) {
                try {
                    val response = request.await()
                    mutableMembershipCard.value = response
                } catch (e: Throwable) {
                    Log.e(LoyaltyWalletRepository::class.simpleName, e.toString())
                }
            }
        }
    }
}