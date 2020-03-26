package com.bink.wallet.modal.card_terms_and_conditions

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.bink.sdk.BinkCore
import com.bink.wallet.BuildConfig
import com.bink.wallet.data.MembershipCardDao
import com.bink.wallet.data.MembershipPlanDao
import com.bink.wallet.data.PaymentCardDao
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
import com.bink.wallet.utils.RELEASE_BUILD_TYPE
import com.bink.wallet.utils.SecurityUtils
import com.bink.wallet.utils.logDebug
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddPaymentCardRepository(
    private val apiService: ApiService,
    private val spreedlyApiService: ApiSpreedly,
    private val paymentCardDao: PaymentCardDao,
    private val membershipCardDao: MembershipCardDao,
    private val membershipPlanDao: MembershipPlanDao
) {

    private val spreedlyKeyMissingError = "Spreedly Environment Key Missing"

    fun sendAddCard(
        context: Context,
        card: PaymentCardAdd,
        cardNumber: String,
        mutableAddCard: MutableLiveData<PaymentCard>,
        error: MutableLiveData<Exception>
    ) {

        //todo hash payment card
        //todo if api v1.2
        card.card.month?.let { safeMonth ->
            card.card.year?.let { safeYear ->
                var paymentCardHash = SecurityUtils.getPaymentCardHash(
                    cardNumber,
                    safeMonth.toString(),
                    safeYear.toString()
                )

//                if (paymentCardHash.isNotEmpty()) {
//                    card.card.hash = paymentCardHash
//                }
                val publicEncryptionKey = LocalStoreUtils.getAppSharedPref(
                    LocalStoreUtils.KEY_ENCRYPT_PAYMENT_PUBLIC_KEY
                )

                // ENCRYPTION TIME

                val encryptedHash = BinkCore(context).sessionConfig.encryptSomething(context, paymentCardHash, publicEncryptionKey)
                val encryptedMonth = BinkCore(context).sessionConfig.encryptSomething(context, safeMonth, publicEncryptionKey)
                val encryptedYear = BinkCore(context).sessionConfig.encryptSomething(context, safeYear, publicEncryptionKey)
                val encryptedfirstSix = BinkCore(context).sessionConfig.encryptSomething(context, card.card.first_six_digits, publicEncryptionKey)
                val encryptedLastFour = BinkCore(context).sessionConfig.encryptSomething(context, card.card.last_four_digits, publicEncryptionKey)

                if (encryptedHash.isNotEmpty()) {
                    card.card.hash = encryptedHash
                }

                if (encryptedMonth.isNotEmpty()) {
                    card.card.month = encryptedMonth
                }

                if (encryptedYear.isNotEmpty()) {
                    card.card.year = encryptedYear
                }

                if (encryptedfirstSix.isNotEmpty()) {
                    card.card.first_six_digits = encryptedfirstSix
                }

                if (encryptedLastFour.isNotEmpty()) {
                    card.card.last_four_digits = encryptedLastFour
                }
            }
        }

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
                        //todo encrypt spreedly
                        val response = spreedlyRequest.await()
                        card.card.apply {
                            token = response.transaction.payment_method.token
                            fingerprint = response.transaction.payment_method.fingerprint
                            first_six_digits = response.transaction.payment_method.first_six_digits
                            last_four_digits = response.transaction.payment_method.last_four_digits
                        }

                        doAddPaymentCard(
                            card,
                            mutableAddCard,
                            error
                        )
                    } catch (e: Exception) {
                        error.value = e
                    }
                }
            }
        } else {
            doAddPaymentCard(card, mutableAddCard, error)
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
        error: MutableLiveData<Exception>
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val request = apiService.addPaymentCardAsync(card)
            withContext(Dispatchers.Main) {
                try {
                    val response = request.await()
                    paymentCardDao.store(response)
                    mutableAddCard.value = response
                } catch (e: Exception) {
                    error.value = e
                }
            }
        }
    }
}