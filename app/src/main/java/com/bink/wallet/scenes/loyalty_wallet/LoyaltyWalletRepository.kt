package com.bink.wallet.scenes.loyalty_wallet

import androidx.lifecycle.MutableLiveData
import com.bink.wallet.data.*
import com.bink.wallet.model.BannerDisplay
import com.bink.wallet.model.request.membership_card.Account
import com.bink.wallet.model.request.membership_card.MembershipCardRequest
import com.bink.wallet.model.request.membership_card.PlanFieldsRequest
import com.bink.wallet.model.response.membershipCardAndPlan.MembershipCardAndPlan
import com.bink.wallet.model.response.membership_card.CardStatus
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.model.response.payment_card.PaymentCard
import com.bink.wallet.network.ApiService
import com.bink.wallet.utils.local_point_scraping.WebScrapableManager
import com.bink.wallet.utils.LocalStoreUtils
import com.bink.wallet.utils.SecurityUtils
import com.bink.wallet.utils.enums.MembershipCardStatus
import com.bink.wallet.utils.generateUuidForMembershipCards
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
        loadCardsError: MutableLiveData<Exception>,
        callback: ((List<MembershipCard>) -> Unit?)? = null
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val membershipCards = apiService.getMembershipCardsAsync()
                withContext(Dispatchers.Main) {
                    processMembershipCardsResult(membershipCards)
                    mutableMembershipCards.value = membershipCards
                    callback?.let { it(membershipCards) }
                }
            } catch (e: Exception) {
                loadCardsError.postValue(e)
            }
        }
    }

    suspend fun retrieveStoredMembershipCards(): List<MembershipCard> {
        return membershipCardDao.getAllAsync()
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
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val membershipPlans = apiService.getMembershipPlansAsync()
                    withContext(Dispatchers.Main) {
                        storeMembershipPlans(membershipPlans, loadPlansError, databaseUpdated)
                        SharedPreferenceManager.membershipPlansLastRequestTime =
                            System.currentTimeMillis()
                        mutableMembershipPlans.value = membershipPlans
                    }
                } catch (e: Exception) {
                    loadPlansError.postValue(e)
                }
            }

        } else {
            retrieveStoredMembershipPlans(mutableMembershipPlans)
        }
    }

    suspend fun retrieveMembershipCardsAndPlans(): MembershipCardAndPlan {
        //Wrap it in a coroutine Scope for the following reasons:
        //To ensure that when the calling scope of this method is cancelled this will also be canceled
        //If any one of the api calls fails,the other one will also fail
        return coroutineScope {
            val membershipPlansRequest = async { apiService.getMembershipPlansAsync() }
            val membershipCardsRequest = async { apiService.getMembershipCardsAsync() }
            val cardsFromDb = retrieveStoredMembershipCards()

            val membershipPlansResult = membershipPlansRequest.await()
            val membershipCardsResult = membershipCardsRequest.await()

            val remappedCards = WebScrapableManager.mapOldToNewCards(cardsFromDb, membershipCardsResult)

            processMembershipCardsResult(remappedCards)

            processMembershipPlansResult(membershipPlansResult)

            MembershipCardAndPlan(remappedCards, membershipPlansResult)
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
            membershipCardDao.deleteCard(id.toString())
            val request = id?.let { apiService.deleteCardAsync(it) }
            withContext(Dispatchers.Main) {
                try {
                    request?.await()
                    mutableDeleteCard.value = id
                } catch (e: Exception) {
                    deleteCardError.value = e
                }
            }
        }
    }


    private fun storeMembershipPlans(
        plans: List<MembershipPlan>,
        loadPlansError: MutableLiveData<Exception> = MutableLiveData(),
        databaseUpdated: MutableLiveData<Boolean>? = MutableLiveData()
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                try {
                    withContext(Dispatchers.IO) { membershipPlanDao.storeAll(plans) }
                    databaseUpdated?.value = true
                } catch (e: Exception) {
                    loadPlansError.value = e
                    logDebug(LoyaltyWalletRepository::class.simpleName, e.toString())
                }
            }
        }
    }

    fun storeMembershipCard(card: MembershipCard) {
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
        createError: MutableLiveData<Exception>,
        addLoyaltyCardRequestMade: MutableLiveData<Boolean>
    ) {

        membershipCardRequest.account?.let { safeAccount ->
            encryptMembershipCardFields(safeAccount)
        }

        CoroutineScope(Dispatchers.IO).launch {
            val request = apiService.createMembershipCardAsync(membershipCardRequest)
            addLoyaltyCardRequestMade.postValue(true)
            withContext(Dispatchers.Main) {
                try {
                    val response = request.await()
                    if (WebScrapableManager.isCardScrapable(response.membership_plan)) {
                        response.status = CardStatus(null, MembershipCardStatus.PENDING.status)
                    }
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
        createCardError: MutableLiveData<Exception>,
        addLoyaltyCardRequestMade: MutableLiveData<Boolean>
    ) {
        membershipCardRequest.account?.let { safeAccount ->
            encryptMembershipCardFields(safeAccount)
        }

        CoroutineScope(Dispatchers.IO).launch {
            val request = apiService.updateMembershipCardAsync(cardId, membershipCardRequest)
            addLoyaltyCardRequestMade.postValue(true)
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
        createCardError: MutableLiveData<Exception>,
        addLoyaltyCardRequestMade: MutableLiveData<Boolean>
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val request = apiService.ghostMembershipCardAsync(cardId, membershipCardRequest)
            addLoyaltyCardRequestMade.postValue(true)
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

    private fun processMembershipCardsResult(membershipCards: List<MembershipCard>?) {
        CoroutineScope(Dispatchers.Default).launch {
            val cardsFromDb = membershipCardDao.getAllAsync()
            val cardIdInDb =
                withContext(Dispatchers.IO) { cardsFromDb }.map { card -> card.id }
            val idFromApi = membershipCards?.map { card -> card.id }

            //list of Id's which are available in database but not in return api
            val difference = idFromApi?.let { cardIdInDb.minus(it) }
            difference?.let {
                if (it.isNotEmpty()) {
                    deleteFromDb(it)
                }
            }

            membershipCards?.let {
                generateUuidForMembershipCards(
                    it,
                    membershipCardDao,
                    paymentCardDao
                )
            }

            val mappedFromScrapedCards = WebScrapableManager.mapOldToNewCards(cardsFromDb, membershipCards)
            membershipCardDao.storeAll(mappedFromScrapedCards)

            SharedPreferenceManager.membershipCardsLastRequestTime =
                System.currentTimeMillis()
        }
    }

    private suspend fun deleteFromDb(cardsToDelete: List<String>) {

        cardsToDelete.forEach { cardIdToDelete ->
            withContext(Dispatchers.IO) {
                membershipCardDao.deleteCard(cardIdToDelete)
            }
        }

    }

    private fun processMembershipPlansResult(membershipPlans: List<MembershipPlan>?) {
        membershipPlans?.let { storeMembershipPlans(it) }
        SharedPreferenceManager.membershipPlansLastRequestTime =
            System.currentTimeMillis()
    }

}