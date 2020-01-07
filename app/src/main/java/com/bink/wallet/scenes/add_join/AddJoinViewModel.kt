package com.bink.wallet.scenes.add_join

import androidx.lifecycle.MutableLiveData
import com.bink.wallet.BaseViewModel
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.model.response.payment_card.PaymentCard
import com.bink.wallet.scenes.pll.PaymentWalletRepository

class AddJoinViewModel constructor(
    private var paymentWalletRepository: PaymentWalletRepository
): BaseViewModel() {
    val membershipPlan = MutableLiveData<MembershipPlan>()
    val paymentCards = MutableLiveData<List<PaymentCard>>()
    val fetchError = MutableLiveData<Throwable>()

    suspend fun getPaymentCards() {
        paymentWalletRepository.getPaymentCards(
            paymentCards,
            fetchError
        )
    }

    fun fetchLocalPaymentCards() {
        paymentWalletRepository.getLocalPaymentCards(
            paymentCards,
            fetchError
        )
    }
}
