package com.bink.wallet.scenes.add_join

import androidx.lifecycle.MutableLiveData
import com.bink.wallet.BaseViewModel
import com.bink.wallet.data.SharedPreferenceManager
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.model.response.payment_card.PaymentCard
import com.bink.wallet.scenes.pll.PaymentWalletRepository
import com.bink.wallet.utils.DateTimeUtils

class AddJoinViewModel constructor(private var paymentWalletRepository: PaymentWalletRepository) :
    BaseViewModel() {
    val membershipPlan = MutableLiveData<MembershipPlan>()
    val paymentCards = MutableLiveData<List<PaymentCard>>()
    val fetchError = MutableLiveData<Exception>()

    fun getPaymentCards() {
        val shouldMakePeriodicCall =
            DateTimeUtils.haveTwoMinutesElapsed(SharedPreferenceManager.paymentCardsLastRequestTime)

        if (shouldMakePeriodicCall) {
            paymentWalletRepository.getPaymentCards(
                paymentCards,
                fetchError
            )
        } else {
            paymentWalletRepository.getLocalPaymentCards(paymentCards, fetchError)
        }
    }

    fun fetchLocalPaymentCards() {
        paymentWalletRepository.getLocalPaymentCards(
            paymentCards,
            fetchError
        )
    }
}
