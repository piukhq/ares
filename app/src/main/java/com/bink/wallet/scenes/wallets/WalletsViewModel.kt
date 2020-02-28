package com.bink.wallet.scenes.wallets

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bink.wallet.BaseViewModel
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.model.response.payment_card.PaymentCard
import com.bink.wallet.scenes.loyalty_wallet.LoyaltyWalletRepository
import com.bink.wallet.scenes.pll.PaymentWalletRepository

class WalletsViewModel(
    private var repository: LoyaltyWalletRepository,
    private var paymentWalletRepository: PaymentWalletRepository
) : BaseViewModel() {

    var membershipPlanData: MutableLiveData<List<MembershipPlan>> = MutableLiveData()
    var membershipCardData: MutableLiveData<List<MembershipCard>> = MutableLiveData()
    val paymentCards = MutableLiveData<List<PaymentCard>>()
    private val _fetchError = MutableLiveData<Throwable>()
    val fetchError: LiveData<Throwable>
        get() = _fetchError
    private val _loadCardsError = MutableLiveData<Throwable>()
    val loadCardsError: LiveData<Throwable>
        get() = _loadCardsError

    fun fetchMembershipCards() {
        repository.retrieveMembershipCards(membershipCardData, _loadCardsError)
    }

    fun fetchMembershipPlans() {
        repository.retrieveStoredMembershipPlans(membershipPlanData)
    }

    fun fetchPaymentCards() {
        paymentWalletRepository.getPaymentCards(
            paymentCards,
            _fetchError
        )
    }
}