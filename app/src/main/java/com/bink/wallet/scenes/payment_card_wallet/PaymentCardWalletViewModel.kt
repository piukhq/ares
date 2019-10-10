package com.bink.wallet.scenes.payment_card_wallet

import androidx.lifecycle.MutableLiveData
import com.bink.wallet.BaseViewModel
import com.bink.wallet.model.response.payment_card.PaymentCard
import com.bink.wallet.scenes.pll.PllRepository
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.scenes.loyalty_wallet.LoyaltyWalletRepository

class PaymentCardWalletViewModel(private var pllRepository: PllRepository) : BaseViewModel() {
    var paymentCards = MutableLiveData<List<PaymentCard>>()
    var fetchError = MutableLiveData<Throwable>()

    suspend fun getPaymentCards() {
        pllRepository.getPaymentCards(paymentCards, fetchError)
    }
}
