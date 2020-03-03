package com.bink.wallet.scenes.add_join

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bink.wallet.BaseViewModel
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.model.response.payment_card.PaymentCard
import com.bink.wallet.scenes.pll.PaymentWalletRepository

class AddJoinViewModel constructor(
    private var paymentWalletRepository: PaymentWalletRepository
) : BaseViewModel() {
    val membershipPlan = MutableLiveData<MembershipPlan>()
    private val _paymentCards = MutableLiveData<List<PaymentCard>>()
    val paymentCards: LiveData<List<PaymentCard>>
        get() = _paymentCards
    val fetchError = MutableLiveData<Throwable>()

    suspend fun getPaymentCards() {
        paymentWalletRepository.getPaymentCards(
            _paymentCards,
            fetchError
        )
    }

    fun fetchLocalPaymentCards() {
        paymentWalletRepository.getLocalPaymentCards(
            _paymentCards,
            fetchError
        )
    }
}
