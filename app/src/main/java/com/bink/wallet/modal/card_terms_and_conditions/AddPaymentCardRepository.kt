package com.bink.wallet.modal.card_terms_and_conditions

import androidx.lifecycle.MutableLiveData
import com.bink.wallet.BuildConfig
import com.bink.wallet.data.MembershipCardDao
import com.bink.wallet.data.MembershipPlanDao
import com.bink.wallet.data.PaymentCardDao
import com.bink.wallet.data.SharedPreferenceManager
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.model.response.payment_card.PaymentCard
import com.bink.wallet.model.response.payment_card.PaymentCardAdd
import com.bink.wallet.model.spreedly.SpreedlyCreditCard
import com.bink.wallet.model.spreedly.SpreedlyPaymentCard
import com.bink.wallet.model.spreedly.SpreedlyPaymentMethod
import com.bink.wallet.network.ApiService
import com.bink.wallet.network.ApiSpreedly
import com.bink.wallet.utils.LocalStoreUtils
import com.bink.wallet.utils.EMPTY_STRING
import com.bink.wallet.utils.RELEASE_BUILD_TYPE
import com.bink.wallet.utils.logDebug
import com.bink.wallet.utils.SecurityUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class AddPaymentCardRepository(
    private val apiService: ApiService,
    private val spreedlyApiService: ApiSpreedly,
    private val paymentCardDao: PaymentCardDao,
    private val membershipCardDao: MembershipCardDao,
    private val membershipPlanDao: MembershipPlanDao
) {

    private val spreedlyKeyMissingError = "Spreedly Environment Key Missing"

    fun sendAddCard(
        card: PaymentCardAdd,
        cardNumber: String,
        mutableAddCard: MutableLiveData<PaymentCard>,
        error: MutableLiveData<Exception>,
        addCardRequestMade: MutableLiveData<Boolean>
    ) {


        // Here we send the users payment card to SpreedlyRetrofit before making a request to Binks API.
        // This can only happen on release, so we have to guard it with the following condition.
        // Another option would be to add an implementation of this class on the /release buildType.
        // However, for this to work we'd have to add the same class across all buildTypes
        // and remove the class from /main. Which seems massively overkill - if anything ever
        // changes, we'll have to remember to update across the project.
        if (BuildConfig.BUILD_TYPE == RELEASE_BUILD_TYPE) {
            val spreedlyEnvironmentKey = LocalStoreUtils.getAppSharedPref(
                LocalStoreUtils.KEY_SPREEDLY
            )?.let {
                it
            }

            if (spreedlyEnvironmentKey == null) {
                error.value = Exception(spreedlyKeyMissingError)
                return
            }

            val spreedlyCreditCard = SpreedlyCreditCard(
                cardNumber,
                card.card.month,
                card.card.year,
                card.card.name_on_card
            )
            val spreedlyPaymentMethod = SpreedlyPaymentMethod(spreedlyCreditCard, "true")
            val spreedlyPaymentCard = SpreedlyPaymentCard(spreedlyPaymentMethod)
            CoroutineScope(Dispatchers.IO).launch {
                val spreedlyRequest = spreedlyApiService.postPaymentCardToSpreedly(
                    spreedlyPaymentCard,
                    spreedlyEnvironmentKey
                )
                withContext(Dispatchers.Main) {
                    try {
                        val response = spreedlyRequest.await()
                        card.card.apply {
                            token = response.transaction.payment_method.token
                            fingerprint = response.transaction.payment_method.fingerprint
                            first_six_digits = response.transaction.payment_method.first_six_digits
                            last_four_digits = response.transaction.payment_method.last_four_digits
                        }

                        encryptCardDetails(card, cardNumber)

                        doAddPaymentCard(
                            card,
                            mutableAddCard,
                            error,
                            addCardRequestMade
                        )
                    } catch (e: Exception) {
                        error.value = e
                    }
                }
            }
        } else {
            encryptCardDetails(card, cardNumber)
            doAddPaymentCard(card, mutableAddCard, error,addCardRequestMade)
        }
    }

    fun retrieveStoredMembershipCards(localMembershipCards: MutableLiveData<List<MembershipCard>>) {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                try {
                    localMembershipCards.value = membershipCardDao.getAllAsync()
                } catch (e: Throwable) {
                    logDebug(AddPaymentCardRepository::class.simpleName, e.toString())
                }
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
                    logDebug(AddPaymentCardRepository::class.simpleName, e.toString())
                }
            }
        }
    }

    private fun doAddPaymentCard(
        card: PaymentCardAdd,
        mutableAddCard: MutableLiveData<PaymentCard>,
        error: MutableLiveData<Exception>,
        addCardRequestMade: MutableLiveData<Boolean>
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val uuid = UUID.randomUUID().toString()
            val request = apiService.addPaymentCardAsync(card)
            SharedPreferenceManager.addPaymentCardRequestUuid = uuid
            addCardRequestMade.postValue(true)
            withContext(Dispatchers.Main) {
                try {
                    val response = request.await()
                    response.uuid = uuid
                    paymentCardDao.store(response)
                    mutableAddCard.value = response
                } catch (e: Exception) {
                    error.value = e
                }
            }
        }
    }

    private fun encryptCardDetails(card: PaymentCardAdd, cardNumber: String) {
        card.card.month?.let { safeMonth ->
            card.card.year?.let { safeYear ->
                var paymentCardHash = SecurityUtils.getPaymentCardHash(
                    cardNumber,
                    safeMonth,
                    safeYear
                )

                val publicEncryptionKey = LocalStoreUtils.getAppSharedPref(
                    LocalStoreUtils.KEY_ENCRYPT_PAYMENT_PUBLIC_KEY
                )

                publicEncryptionKey?.let { safeKey ->
                    val encryptedHash = SecurityUtils.encryptMessage(
                        paymentCardHash,
                        publicEncryptionKey
                    )
                    val encryptedMonth =
                        SecurityUtils.encryptMessage(safeMonth, safeKey)

                    val encryptedYear =
                        SecurityUtils.encryptMessage(safeYear, publicEncryptionKey)

                    var encryptedFirstSix = EMPTY_STRING

                    card.card.first_six_digits?.let { safeSixDigits ->
                        encryptedFirstSix = SecurityUtils.encryptMessage(
                            safeSixDigits,
                            publicEncryptionKey
                        )
                    }

                    var encryptedLastFour = EMPTY_STRING

                    card.card.last_four_digits?.let { safeFourDigits ->
                        encryptedLastFour = SecurityUtils.encryptMessage(
                            safeFourDigits,
                            publicEncryptionKey
                        )
                    }

                    if (encryptedHash.isNotEmpty()) {
                        card.card.hash = encryptedHash
                    }

                    if (encryptedMonth.isNotEmpty()) {
                        card.card.month = encryptedMonth
                    }

                    if (encryptedYear.isNotEmpty()) {
                        card.card.year = encryptedYear
                    }

                    if (encryptedFirstSix.isNotEmpty()) {
                        card.card.first_six_digits = encryptedFirstSix
                    }

                    if (encryptedLastFour.isNotEmpty()) {
                        card.card.last_four_digits = encryptedLastFour
                    }
                }
            }
        }
    }
}