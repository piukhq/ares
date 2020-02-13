package com.bink.wallet.modal.card_terms_and_conditions

import android.util.Log
import androidx.lifecycle.MutableLiveData
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
    fun sendAddCard(card: PaymentCardAdd,
                    mutableAddCard: MutableLiveData<PaymentCard>,
                    error: MutableLiveData<Throwable>
    ) {

        val spreedlyCreditCard = SpreedlyCreditCard("5356701010417006", 7, 2022, "Connor McFadden")
        val spreedlyPaymentMethod = SpreedlyPaymentMethod(spreedlyCreditCard, "true")
        val spreedlyPaymentCard = SpreedlyPaymentCard(spreedlyPaymentMethod)
        CoroutineScope(Dispatchers.IO).launch {
            Log.e("ConnorDebug", "req send add card")
            val spreedlyRequest = spreedlyApiService.postPaymentCardToSpreedly(spreedlyPaymentCard)
            withContext(Dispatchers.Main) {
                try {
                    val response = spreedlyRequest.await()
                    Log.e("ConnorDebug", "response:: token: " + response.transaction.payment_method.token)
                    // make a req to bink
                } catch (e: Throwable) {
                    Log.e("ConnorDebug", "error: " + e.localizedMessage)
//                    error.value = e
                }
            }

//            val request = apiService.addPaymentCardAsync(card)
//            withContext(Dispatchers.Main) {
//                try {
//                    val response = request.await()
//                    paymentCardDao.store(response)
//                    mutableAddCard.value = response
//                } catch (e: Throwable) {
//                    error.value = e
//                }
//            }
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
}