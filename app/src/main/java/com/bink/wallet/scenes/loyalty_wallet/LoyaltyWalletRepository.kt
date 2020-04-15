package com.bink.wallet.scenes.loyalty_wallet

import androidx.lifecycle.MutableLiveData
import com.bink.wallet.data.*
import com.bink.wallet.model.BannerDisplay
import com.bink.wallet.model.request.membership_card.Account
import com.bink.wallet.model.request.membership_card.MembershipCardRequest
import com.bink.wallet.model.request.membership_card.PlanFieldsRequest
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.model.response.payment_card.PaymentCard
import com.bink.wallet.model.response.payment_card.PaymentCardAdd
import com.bink.wallet.network.ApiService
import com.bink.wallet.utils.EMPTY_STRING
import com.bink.wallet.utils.LocalStoreUtils
import com.bink.wallet.utils.SecurityUtils
import com.bink.wallet.utils.enums.BackendVersion
import com.bink.wallet.utils.logDebug
import kotlinx.coroutines.*


class LoyaltyWalletRepository(
    private val apiService: ApiService,
    private val membershipCardDao: MembershipCardDao,
    private val membershipPlanDao: MembershipPlanDao,
    private val bannersDisplayDao: BannersDisplayDao,
    private val paymentCardDao: PaymentCardDao
) {


    fun retrieveMembershipCards(
        mutableMembershipCards: MutableLiveData<List<MembershipCard>>,
        loadCardsError: MutableLiveData<Exception>
    ) {
        val request = apiService.getMembershipCardsAsync()
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                try {
                    val response = request.await()
                    runBlocking {
                        membershipCardDao.deleteAllCards()
                        membershipCardDao.storeAll(response)

                        SharedPreferenceManager.membershipCardsLastRequestTime =
                            System.currentTimeMillis()

                        mutableMembershipCards.value = response.toMutableList()
                    }
                } catch (e: Exception) {
                    loadCardsError.value = e
                }
            }
        }
    }

    fun retrieveStoredMembershipCards(localMembershipCards: MutableLiveData<List<MembershipCard>>) {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                try {
                    localMembershipCards.value = membershipCardDao.getAllAsync()
                } catch (e: Exception) {
                    logDebug(LoyaltyWalletRepository::class.simpleName, e.toString())
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
                } catch (e: Exception) {
                    logDebug(LoyaltyWalletRepository::class.simpleName, e.toString())
                }
            }
        }
    }

    fun retrieveMembershipPlans(
        mutableMembershipPlans: MutableLiveData<List<MembershipPlan>>,
        loadPlansError: MutableLiveData<Exception>,
        fromPersistence: Boolean = false
    ) {
        retrieveMembershipPlans(mutableMembershipPlans, loadPlansError, null, fromPersistence)
    }

    fun retrieveMembershipPlans(
        mutableMembershipPlans: MutableLiveData<List<MembershipPlan>>,
        loadPlansError: MutableLiveData<Exception>,
        databaseUpdated: MutableLiveData<Boolean>?,
        fromPersistence: Boolean = false
    ) {
        if (!fromPersistence) {
            val request = apiService.getMembershipPlansAsync()
            CoroutineScope(Dispatchers.IO).launch {
                withContext(Dispatchers.Main) {
                    try {
                        val response = request.await()
                        storeMembershipPlans(response, loadPlansError, databaseUpdated)
                        SharedPreferenceManager.membershipPlansLastRequestTime =
                            System.currentTimeMillis()
                        mutableMembershipPlans.value = response.toMutableList()
                    } catch (e: Exception) {
                        loadPlansError.value = e
                    }
                }
            }
        } else {
            retrieveStoredMembershipPlans(mutableMembershipPlans)
        }
    }

    fun retrieveStoredMembershipPlans(localMembershipPlans: MutableLiveData<List<MembershipPlan>>) {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                try {
                    val response = membershipPlanDao.getAllAsync()
                    localMembershipPlans.value = response
                } catch (e: Exception) {
                    // TODO: Have error catching here in a mutable
                    logDebug(LoyaltyWalletRepository::class.simpleName, e.toString())
                }
            }
        }
    }

    fun deleteMembershipCard(
        id: String?,
        mutableDeleteCard: MutableLiveData<String>,
        deleteCardError: MutableLiveData<Exception>
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val request = id?.let { apiService.deleteCardAsync(it) }
            withContext(Dispatchers.Main) {
                try {
                    request?.await()
                    mutableDeleteCard.value = id
                    membershipCardDao.deleteCard(id.toString())
                } catch (e: Exception) {
                    deleteCardError.value = e
                }
            }
        }
    }

    private fun storeMembershipPlans(
        plans: List<MembershipPlan>,
        loadPlansError: MutableLiveData<Exception>,
        databaseUpdated: MutableLiveData<Boolean>?
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                try {
                    withContext(Dispatchers.IO) { membershipPlanDao.storeAll(plans) }
                    databaseUpdated?.value = true
                } catch (e: Exception) {
                    // TODO: Have error catching here in a mutable
                    loadPlansError.value = e
                    logDebug(LoyaltyWalletRepository::class.simpleName, e.toString())
                }
            }
        }
    }

    private fun storeMembershipCard(card: MembershipCard) {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                try {
                    withContext(Dispatchers.IO) {
                        membershipCardDao.storeMembershipCard(card)
                    }
                } catch (e: Exception) {
                    logDebug(LoyaltyWalletRepository::class.simpleName, e.toString())
                }
            }
        }
    }

    fun createMembershipCard(
        membershipCardRequest: MembershipCardRequest,
        mutableMembershipCard: MutableLiveData<MembershipCard>,
        createError: MutableLiveData<Exception>
    ) {

        val cachedBackendVersion = SharedPreferenceManager.storedBackendVersion
        if (cachedBackendVersion != null
            && cachedBackendVersion == BackendVersion.VERSION_2.version
        ) {
            membershipCardRequest.account?.let { safeAccount ->
                encryptMembershipCardFields(safeAccount)
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            val request = apiService.createMembershipCardAsync(membershipCardRequest)
            withContext(Dispatchers.Main) {
                try {
                    val response = request.await()
                    storeMembershipCard(response)
                    mutableMembershipCard.value = response
                } catch (e: Exception) {
                    createError.value = e
                }
            }
        }
    }

    fun updateMembershipCard(
        cardId: String,
        membershipCardRequest: MembershipCardRequest,
        membershipCardData: MutableLiveData<MembershipCard>,
        createCardError: MutableLiveData<Exception>
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val request = apiService.updateMembershipCardAsync(cardId, membershipCardRequest)
            withContext(Dispatchers.Main) {
                try {
                    val response = request.await()
                    membershipCardData.value = response
                } catch (e: Exception) {
                    createCardError.value = e
                }
            }
        }
    }

    fun ghostMembershipCard(
        cardId: String,
        membershipCardRequest: MembershipCardRequest,
        membershipCardData: MutableLiveData<MembershipCard>,
        createCardError: MutableLiveData<Exception>
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val request = apiService.ghostMembershipCardAsync(cardId, membershipCardRequest)
            withContext(Dispatchers.Main) {
                try {
                    val response = request.await()
                    membershipCardData.value = response
                } catch (e: Exception) {
                    createCardError.value = e
                }
            }
        }
    }

    fun getPaymentCards(
        paymentCards: MutableLiveData<List<PaymentCard>>,
        fetchError: MutableLiveData<Exception>
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val request = apiService.getPaymentCardsAsync()
            withContext(Dispatchers.Main) {
                try {
                    val response = request.await()
                    paymentCards.postValue(response)
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

    fun addBannerAsDismissed(
        id: String,
        addError: MutableLiveData<Exception>,
        dismissedBannerDisplay: MutableLiveData<String> = MutableLiveData()
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                try {
                    bannersDisplayDao.addBannerAsDismissed(BannerDisplay(id))
                    dismissedBannerDisplay.value = id
                } catch (e: Exception) {
                    addError.value = e
                }
            }
        }
    }

    fun retrieveDismissedCards(
        localMembershipCards: MutableLiveData<List<BannerDisplay>>,
        fetchError: MutableLiveData<Exception>
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                try {
                    localMembershipCards.value = bannersDisplayDao.getDismissedBanners()
                } catch (e: Exception) {
                    fetchError.value = e
                }
            }
        }
    }

    fun getLocalData(
        localMembershipPlans: MutableLiveData<List<MembershipPlan>>,
        localMembershipCards: MutableLiveData<List<MembershipCard>>,
        localDismissedMembershipCards: MutableLiveData<List<BannerDisplay>>,
        fetchError: MutableLiveData<Exception>,
        updateDone: MutableLiveData<Boolean>
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                try {
                    val storedMembershipPlans =
                        async(Dispatchers.IO) { membershipPlanDao.getAllAsync() }
                    val storedMembershipCards =
                        async(Dispatchers.IO) { membershipCardDao.getAllAsync() }
                    val storedDismissedBanners =
                        async(Dispatchers.IO) { bannersDisplayDao.getDismissedBanners() }

                    localDismissedMembershipCards.value = storedDismissedBanners.await()
                    localMembershipCards.value = storedMembershipCards.await()
                    localMembershipPlans.value = storedMembershipPlans.await()
                    updateDone.value = true
                } catch (exception: Exception) {
                    fetchError.value = exception
                }
            }
        }
    }

    private fun encryptMembershipCardFields(account: Account) {
        doEncryptionMembershipCardFields(account.registration_fields)
        doEncryptionMembershipCardFields(account.add_fields)
        doEncryptionMembershipCardFields(account.authorise_fields)
        doEncryptionMembershipCardFields(account.enrol_fields)
        doEncryptionMembershipCardFields(account.plan_documents)
    }

    private fun doEncryptionMembershipCardFields(fields: MutableList<PlanFieldsRequest>?) {
        val publicEncryptionKey = LocalStoreUtils.getAppSharedPref(
            LocalStoreUtils.KEY_ENCRYPT_PAYMENT_PUBLIC_KEY
        )

        publicEncryptionKey?.let { safePubKey ->
            fields?.let { safeAddAuthFields ->
                for (planFieldRequest: PlanFieldsRequest in safeAddAuthFields) {
                    if (planFieldRequest.isSensitive) {
                        planFieldRequest.value?.let { safeValue ->
                            val encryptedValue =
                                SecurityUtils.encryptMessage(safeValue, safePubKey)
                            planFieldRequest.value = encryptedValue
                        }

                    }
                }
            }
        }
    }
}