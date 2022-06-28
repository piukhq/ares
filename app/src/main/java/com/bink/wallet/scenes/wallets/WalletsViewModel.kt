package com.bink.wallet.scenes.wallets

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bink.wallet.BaseViewModel
import com.bink.wallet.data.SharedPreferenceManager
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.model.response.payment_card.PaymentCard
import com.bink.wallet.scenes.loyalty_wallet.wallet.LoyaltyWalletRepository
import com.bink.wallet.scenes.pll.PaymentWalletRepository
import com.bink.wallet.utils.DateTimeUtils

class WalletsViewModel(
    private var repository: LoyaltyWalletRepository,
    private var paymentWalletRepository: PaymentWalletRepository
) : BaseViewModel() {

    var membershipPlanData: MutableLiveData<List<MembershipPlan>> = MutableLiveData()
    var membershipCardData: MutableLiveData<List<MembershipCard>> = MutableLiveData()
    val paymentCards = MutableLiveData<List<PaymentCard>>()
    private val _fetchError = MutableLiveData<Exception>()
    val fetchError: LiveData<Exception>
        get() = _fetchError
    private val _loadCardsError = MutableLiveData<Exception>()

    fun fetchMembershipCards() {
        val shouldMakePeriodicCall =
            DateTimeUtils.haveTwoMinutesElapsed(SharedPreferenceManager.membershipCardsLastRequestTime)

        if (shouldMakePeriodicCall) {
            repository.retrieveMembershipCards(membershipCardData, _loadCardsError)
        }
    }

    fun fetchStoredMembershipPlans() {
        repository.retrieveStoredMembershipPlans(membershipPlanData)
    }

    fun fetchPaymentCards() {
        val shouldMakePeriodicCall =
            DateTimeUtils.haveTwoMinutesElapsed(SharedPreferenceManager.paymentCardsLastRequestTime)

        if (shouldMakePeriodicCall) {
            paymentWalletRepository.getPaymentCards(
                paymentCards,
                _fetchError
            )
        }
    }
}