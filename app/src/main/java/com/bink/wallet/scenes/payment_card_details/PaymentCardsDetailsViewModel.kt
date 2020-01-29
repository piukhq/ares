package com.bink.wallet.scenes.payment_card_details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bink.wallet.BaseViewModel
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.model.response.payment_card.PaymentCard
import com.bink.wallet.scenes.loyalty_wallet.LoyaltyWalletRepository
import com.bink.wallet.scenes.pll.PaymentWalletRepository
import okhttp3.ResponseBody

class PaymentCardsDetailsViewModel(
    private var paymentWalletRepository: PaymentWalletRepository,
    private var loyaltyWalletRepository: LoyaltyWalletRepository
) :
    BaseViewModel() {

    val paymentCard = MutableLiveData<PaymentCard>()
    val membershipCardData: MutableLiveData<List<MembershipCard>> = MutableLiveData()
    val membershipPlanData: MutableLiveData<List<MembershipPlan>> = MutableLiveData()
    val linkedPaymentCard = MutableLiveData<PaymentCard>()
    val unlinkedRequestBody = MutableLiveData<ResponseBody>()
    val deleteRequest = MutableLiveData<ResponseBody>()

    private val _loadCardsError = MutableLiveData<Throwable>()
    val loadCardsError: LiveData<Throwable>
        get() = _loadCardsError

    private val _linkError = MutableLiveData<Throwable>()
    val linkError: LiveData<Throwable>
        get() = _linkError

    private val _unlinkError = MutableLiveData<Throwable>()
    val unlinkError: LiveData<Throwable>
        get() = _unlinkError

    private var _deleteError = MutableLiveData<Throwable>()
    val deleteError: LiveData<Throwable>
        get() = _deleteError

    fun linkPaymentCard(cardId: String, paymentCardId: String) {
        paymentWalletRepository.linkPaymentCard(
            cardId,
            paymentCardId,
            _linkError,
            paymentCard
        )
    }

    fun unlinkPaymentCard(cardId: String, paymentCardId: String) {
        paymentWalletRepository.unlinkPaymentCard(
            paymentCardId,
            cardId,
            _unlinkError,
            unlinkedRequestBody,
            paymentCard
        )
    }

    fun deletePaymentCard(paymentCardId: String) {
        paymentWalletRepository.deletePaymentCard(paymentCardId, deleteRequest, _deleteError)
    }

    fun getMembershipCards() {
        loyaltyWalletRepository.retrieveMembershipCards(membershipCardData, _loadCardsError)
    }

    fun getPaymentCard(paymentCardId: String) {
        paymentWalletRepository.getPaymentCard(paymentCardId, paymentCard)
    }
}
