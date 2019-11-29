package com.bink.wallet.scenes.payment_card_wallet

import androidx.lifecycle.MutableLiveData
import com.bink.wallet.BaseViewModel
import com.bink.wallet.model.BannerDisplay
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.model.response.payment_card.PaymentCard
import com.bink.wallet.scenes.loyalty_wallet.LoyaltyWalletRepository
import com.bink.wallet.scenes.pll.PaymentWalletRepository
import okhttp3.ResponseBody

class PaymentCardWalletViewModel(
    private var paymentWalletRepository: PaymentWalletRepository,
    private var loyaltyWalletRepository: LoyaltyWalletRepository
) : BaseViewModel() {
    val paymentCards = MutableLiveData<List<PaymentCard>>()
    val fetchError = MutableLiveData<Throwable>()
    val deleteCard = MutableLiveData<String>()
    val deleteRequest = MutableLiveData<ResponseBody>()
    val deleteError = MutableLiveData<Throwable>()
    val localMembershipPlanData = MutableLiveData<List<MembershipPlan>>()
    val localMembershipCardData = MutableLiveData<List<MembershipCard>>()
    val dismissedCardData: MutableLiveData<List<BannerDisplay>> = MutableLiveData()
    val addError = MutableLiveData<Throwable>()

    suspend fun deleteCard(id: String?) {
        loyaltyWalletRepository.deleteMembershipCard(
            id,
            deleteCard
        )
    }

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

    fun fetchLocalMembershipCards() {
        loyaltyWalletRepository.retrieveStoredMembershipCards(
            localMembershipCardData
        )
    }

    fun fetchLocalMembershipPlans() {
        loyaltyWalletRepository.retrieveStoredMembershipPlans(
            localMembershipPlanData
        )
    }

    suspend fun deletePaymentCard(paymentCardId: String) {
        paymentWalletRepository.deletePaymentCard(paymentCardId, deleteRequest, deleteError)
    }


    fun fetchDismissedCards() {
        loyaltyWalletRepository.retrieveDismissedCards(dismissedCardData, fetchError)
    }

    fun addPlanIdAsDismissed(id: String) {
        loyaltyWalletRepository.addBannerAsDismissed(id, addError)
    }
}
