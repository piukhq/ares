package com.bink.wallet.scenes.wallets

import androidx.lifecycle.LiveData
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
    private val _fetchError = MutableLiveData<Throwable>()
    val fetchError : LiveData<Throwable>
        get() = _fetchError
    private val _loadCardsError = MutableLiveData<Throwable>()
    val loadCardsError : LiveData<Throwable>
        get() = _loadCardsError

    fun fetchLocalMembershipPlans() {
        repository.retrieveStoredMembershipPlans(membershipPlanData)
    }

     fun fetchMembershipCards() {
        viewModelScope.launch {
            repository.retrieveMembershipCards(membershipCardData, _loadCardsError)
        }
    }

    fun fetchMembershipPlans() {
        viewModelScope.launch {
            repository.retrieveMembershipPlans(membershipPlanData, _loadCardsError)
        }
    }

     fun fetchPaymentCards() {
        paymentWalletRepository.getPaymentCards(
            paymentCards,
            _fetchError
        )
    }
}