package com.bink.wallet.scenes.payment_card_wallet

import androidx.lifecycle.MutableLiveData
import com.bink.wallet.BaseViewModel
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.model.response.payment_card.PaymentCard
import com.bink.wallet.scenes.loyalty_wallet.LoyaltyWalletRepository
import com.bink.wallet.scenes.pll.PllRepository

class PaymentCardWalletViewModel(
    private var pllRepository: PllRepository,
    private var loyaltyWalletRepository: LoyaltyWalletRepository
) : BaseViewModel() {
    var paymentCards = MutableLiveData<List<PaymentCard>>()
    var fetchError = MutableLiveData<Throwable>()
    var deleteCard: MutableLiveData<String> = MutableLiveData()
    var localMembershipPlanData: MutableLiveData<List<MembershipPlan>> = MutableLiveData()
    var localMembershipCardData: MutableLiveData<List<MembershipCard>> = MutableLiveData()

    suspend fun deleteCard(id: String?) {
        loyaltyWalletRepository.deleteMembershipCard(id, deleteCard)
    }

    fun fetchLocalMembershipCards() {
        loyaltyWalletRepository.retrieveStoredMembershipCards(localMembershipCardData)
    }

    fun fetchLocalMembershipPlans() {
        loyaltyWalletRepository.retrieveStoredMembershipPlans(localMembershipPlanData)
    }

    suspend fun getPaymentCards() {
        pllRepository.getPaymentCards(paymentCards, fetchError)
    }
}
