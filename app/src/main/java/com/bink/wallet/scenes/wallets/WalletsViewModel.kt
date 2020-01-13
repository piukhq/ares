package com.bink.wallet.scenes.wallets

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bink.wallet.BaseViewModel
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.model.response.payment_card.PaymentCard
import com.bink.wallet.scenes.loyalty_wallet.LoyaltyWalletRepository
import com.bink.wallet.scenes.pll.PaymentWalletRepository
import kotlinx.coroutines.launch

class WalletsViewModel(
    private var repository: LoyaltyWalletRepository,
    private var paymentWalletRepository: PaymentWalletRepository
) : BaseViewModel() {

    var membershipPlanData: MutableLiveData<List<MembershipPlan>> = MutableLiveData()
    var membershipCardData: MutableLiveData<List<MembershipCard>> = MutableLiveData()
    val paymentCards = MutableLiveData<List<PaymentCard>>()
    val fetchError = MutableLiveData<Throwable>()

    fun fetchLocalMembershipPlans() {
        repository.retrieveStoredMembershipPlans(membershipPlanData)
    }

    suspend fun fetchMembershipCards() {
        viewModelScope.launch {
            try {
                repository.retrieveMembershipCards(membershipCardData)
            } catch (e: Exception) {
                onLoadFail(e)
            }
        }
    }

    suspend fun fetchMembershipPlans() {
        viewModelScope.launch {
            try {
                repository.retrieveMembershipPlans(membershipPlanData)
            } catch (e: Exception) {
                onLoadFail(e)
            }
        }
    }

    suspend fun fetchPaymentCards() {
        paymentWalletRepository.getPaymentCards(
            paymentCards,
            fetchError
        )
    }
}