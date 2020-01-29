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
import com.bink.wallet.network.ApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CardTermsAndConditionsRepository(
    private val apiService: ApiService,
    private val paymentCardDao: PaymentCardDao,
    private val membershipCardDao: MembershipCardDao,
    private val membershipPlanDao: MembershipPlanDao
) {
    fun sendAddCard(card: PaymentCardAdd,
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