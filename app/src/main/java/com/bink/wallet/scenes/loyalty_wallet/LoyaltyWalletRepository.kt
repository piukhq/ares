package com.bink.wallet.scenes.loyalty_wallet

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.bink.wallet.data.BannersDisplayDao
import com.bink.wallet.data.MembershipCardDao
import com.bink.wallet.data.MembershipPlanDao
import com.bink.wallet.model.BannerDisplay
import com.bink.wallet.model.request.membership_card.MembershipCardRequest
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.model.response.payment_card.PaymentCard
import com.bink.wallet.network.ApiService
import kotlinx.coroutines.*


class LoyaltyWalletRepository(
    private val apiService: ApiService,
    private val membershipCardDao: MembershipCardDao,
    private val membershipPlanDao: MembershipPlanDao,
    private val bannersDisplayDao: BannersDisplayDao
) {

    suspend fun retrieveMembershipCards(
        mutableMembershipCards: MutableLiveData<List<MembershipCard>>,
        loadCardsError: MutableLiveData<Throwable>
    ) {
        val request = apiService.getMembershipCardsAsync()
        withContext(Dispatchers.Main) {
            try {
                val response = request.await()
                storeMembershipCards(response)
                mutableMembershipCards.postValue(response.toMutableList())
            } catch (e: Exception) {
                loadCardsError.value = e
            }
        }
    }

    fun retrieveStoredMembershipCards(localMembershipCards: MutableLiveData<List<MembershipCard>>) {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                try {
                    localMembershipCards.value = membershipCardDao.getAllAsync()
                } catch (e: Throwable) {
                    Log.e(LoyaltyWalletRepository::class.simpleName, e.toString())
                }
            }
        }
    }

    fun clearMembershipCards() {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                try {
                    membershipCardDao.deleteAllCards()
                    membershipPlanDao.deleteAllPlans()
                } catch (e: Throwable) {
                    Log.e(LoyaltyWalletRepository::class.simpleName, e.toString())
                }
            }
        }
    }

    suspend fun retrieveMembershipPlans(
        mutableMembershipPlans: MutableLiveData<List<MembershipPlan>>,
        loadPlansError: MutableLiveData<Throwable>
    ) {
        val request = apiService.getMembershipPlansAsync()
        withContext(Dispatchers.Main) {
            try {
                val response = request.await()
                storeMembershipPlans(response)
                mutableMembershipPlans.value = response.toMutableList()
            } catch (e: java.lang.Exception) {
                loadPlansError.value = e
            }
        }
    }

    fun retrieveStoredMembershipPlans(localMembershipPlans: MutableLiveData<List<MembershipPlan>>) {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                try {
                    val response = membershipPlanDao.getAllAsync()
                    localMembershipPlans.value = response
                } catch (e: Throwable) {
                    Log.e(LoyaltyWalletRepository::class.simpleName, e.toString())
                }
            }
        }
    }

    suspend fun deleteMembershipCard(id: String?, mutableDeleteCard: MutableLiveData<String>) {
        CoroutineScope(Dispatchers.IO).launch {
            val request = id?.let { apiService.deleteCardAsync(it) }
            withContext(Dispatchers.Main) {
                try {
                    request?.await()
                    mutableDeleteCard.value = id
                    membershipCardDao.deleteCard(id.toString())
                } catch (e: Throwable) {
                    Log.e(LoyaltyWalletRepository::class.simpleName, e.toString())
                }
            }
        }
    }

    private fun storeMembershipPlans(plans: List<MembershipPlan>) {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                try {
                    runBlocking {
                        membershipPlanDao.storeAll(plans)
                    }
                } catch (e: Throwable) {
                    Log.e(LoyaltyWalletRepository::class.simpleName, e.toString())
                }
            }
        }
    }

    private fun storeMembershipCards(cards: List<MembershipCard>) {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                try {
                    runBlocking {
                        membershipCardDao.deleteAllCards()
                        membershipCardDao.storeAll(cards)
                    }
                } catch (e: Throwable) {
                    Log.e(LoyaltyWalletRepository::class.simpleName, e.toString())
                }
            }
        }
    }

    fun createMembershipCard(
        membershipCardRequest: MembershipCardRequest,
        mutableMembershipCard: MutableLiveData<MembershipCard>,
        createError: MutableLiveData<String>
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val request = apiService.createMembershipCardAsync(membershipCardRequest)
            withContext(Dispatchers.Main) {
                try {
                    val response = request.await()
                    mutableMembershipCard.value = response
                } catch (e: Throwable) {
                    createError.value = e.localizedMessage
                    Log.e(LoyaltyWalletRepository::class.simpleName, e.toString())
                }
            }
        }
    }

    fun updateMembershipCard(
        cardId: String,
        membershipCardRequest: MembershipCardRequest,
        membershipCardData: MutableLiveData<MembershipCard>,
        createCardError: MutableLiveData<String>
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val request = apiService.updateMembershipCardAsync(cardId, membershipCardRequest)
            withContext(Dispatchers.Main) {
                try {
                    val response = request.await()
                    membershipCardData.value = response
                } catch (e: Throwable) {
                    createCardError.value = e.localizedMessage
                    Log.e(LoyaltyWalletRepository::class.simpleName, e.toString())
                }
            }
        }
    }

    fun ghostMembershipCard(
        cardId: String,
        membershipCardRequest: MembershipCardRequest,
        membershipCardData: MutableLiveData<MembershipCard>,
        createCardError: MutableLiveData<String>
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val request = apiService.ghostMembershipCardAsync(cardId, membershipCardRequest)
            withContext(Dispatchers.Main) {
                try {
                    val response = request.await()
                    membershipCardData.value = response
                } catch (e: Throwable) {
                    createCardError.value = e.localizedMessage
                    Log.e(LoyaltyWalletRepository::class.simpleName, e.toString())
                }
            }
        }
    }

    suspend fun getPaymentCards(
        paymentCards: MutableLiveData<List<PaymentCard>>,
        fetchError: MutableLiveData<Throwable>
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val request = apiService.getPaymentCardsAsync()
            withContext(Dispatchers.Main) {
                try {
                    val response = request.await()
                    paymentCards.postValue(response)
                } catch (e: Throwable) {
                    fetchError.value = e
                    Log.e(LoyaltyWalletRepository::class.simpleName, e.toString())
                }
            }
        }
    }

    fun addBannerAsDismissed(
        id: String,
        addError: MutableLiveData<Throwable>,
        dismissedBannerDisplay: MutableLiveData<String> = MutableLiveData()
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                try {
                    bannersDisplayDao.addBannerAsDismissed(BannerDisplay(id))
                    dismissedBannerDisplay.value = id
                } catch (e: Throwable) {
                    addError.value = e
                }
            }
        }
    }

    fun retrieveDismissedCards(
        localMembershipCards: MutableLiveData<List<BannerDisplay>>,
        fetchError: MutableLiveData<Throwable>
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                try {
                    localMembershipCards.value = bannersDisplayDao.getDismissedBanners()
                } catch (e: Throwable) {
                    fetchError.value = e
                }
            }
        }
    }
}