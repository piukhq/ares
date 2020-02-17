package com.bink.wallet.modal.card_terms_and_conditions

import android.util.Log
import androidx.lifecycle.MutableLiveData
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CardTermsAndConditionsRepository(
    private val apiService: ApiService,
    private val spreedlyApiService: ApiSpreedly,
    private val paymentCardDao: PaymentCardDao,
    private val membershipCardDao: MembershipCardDao,
    private val membershipPlanDao: MembershipPlanDao
) {
    fun sendAddCard(
        card: PaymentCardAdd,
        cardNumber: String,
        mutableAddCard: MutableLiveData<PaymentCard>,
        error: MutableLiveData<Throwable>
    ) {

        // Here we send the users payment card to Spreedly before making a request to Binks API.
        // This can only happen on release, so we have to guard it with the following condition.
        // Another option would be to add an implementation of this class on the /release buildType.
        // However, for this to work we'd have to add the same class across all buildTypes
        // and remove the class from /main. Which seems massively overkill - if anything ever
        // changes, we'll have to remember to update across the project.
        if (BuildConfig.BUILD_TYPE == "release") {
            val spreedlyEnvironmentKey = LocalStoreUtils.getAppSharedPref(
                LocalStoreUtils.KEY_SPREEDLY
            )?.let {
                it
            }

            //todo safety checks
            val spreedlyCreditCard = SpreedlyCreditCard(
                cardNumber,
                card.card.month!!,
                card.card.year!!,
                card.card.name_on_card!!
            )
            val spreedlyPaymentMethod = SpreedlyPaymentMethod(spreedlyCreditCard, "true")
            val spreedlyPaymentCard = SpreedlyPaymentCard(spreedlyPaymentMethod)
            CoroutineScope(Dispatchers.IO).launch {
                val spreedlyRequest = spreedlyApiService.postPaymentCardToSpreedly(
                    spreedlyPaymentCard,
                    spreedlyEnvironmentKey!!
                )
                withContext(Dispatchers.Main) {
                    try {
                        val response = spreedlyRequest.await()
                        card.card.token = response.transaction.payment_method.token
                        card.card.fingerprint = response.transaction.payment_method.fingerprint
                        card.card.first_six_digits =
                            response.transaction.payment_method.first_six_digits
                        card.card.last_four_digits =
                            response.transaction.payment_method.last_four_digits

                        doAddPaymentCard(card, mutableAddCard, error)
                    } catch (e: Throwable) {
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
                    Log.d(CardTermsAndConditionsRepository::class.simpleName, e.toString())
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
                    Log.d(CardTermsAndConditionsRepository::class.simpleName, e.toString())
                }
            }
        }
    }

    private fun doAddPaymentCard(
        card: PaymentCardAdd,
        mutableAddCard: MutableLiveData<PaymentCard>,
        error: MutableLiveData<Throwable>
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val request = apiService.addPaymentCardAsync(card)
            withContext(Dispatchers.Main) {
                try {
                    val response = request.await()
                    paymentCardDao.store(response)
                    mutableAddCard.value = response
                } catch (e: Throwable) {
                    error.value = e
                }
            }
        }
    }
}