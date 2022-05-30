package com.bink.wallet.scenes.payment_card_wallet

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
import okhttp3.ResponseBody

class PaymentCardWalletViewModel(
    private var paymentWalletRepository: PaymentWalletRepository,
    private var loyaltyWalletRepository: LoyaltyWalletRepository
) : BaseViewModel() {
    val paymentCards = MutableLiveData<List<PaymentCard>>()
    val deleteCard = MutableLiveData<String>()
    val deleteRequest = MutableLiveData<ResponseBody>()
    val localMembershipPlanData = MutableLiveData<List<MembershipPlan>>()
    val localMembershipCardData = MutableLiveData<List<MembershipCard>>()

    private val _fetchError = MutableLiveData<Exception>()
    val fetchError: LiveData<Exception> get() = _fetchError
    private val _deleteError = MutableLiveData<Exception>()
    val deleteError: LiveData<Exception> get() = _deleteError
    private val _deleteCardError = MutableLiveData<Exception>()
    val deleteCardError: LiveData<Exception> get() = _deleteCardError
    private val _addError = MutableLiveData<Exception>()
    val addError: LiveData<Exception> get() = _addError
    val loyaltyUpdateDone = MutableLiveData<Boolean>()
    val paymentUpdateDone = MutableLiveData<Boolean>()

    fun deleteCard(id: String?) {
        loyaltyWalletRepository.deleteMembershipCard(
            id,
            deleteCard,
            _deleteCardError
        )
    }

    fun getPaymentCards() {
        paymentWalletRepository.getPaymentCards(
            paymentCards,
            _fetchError
        )
    }

    fun getPeriodicPaymentCards() {
        val shouldMakePeriodicCall =
            DateTimeUtils.haveTwoMinutesElapsed(SharedPreferenceManager.paymentCardsLastRequestTime)

        if (shouldMakePeriodicCall) {
            getPaymentCards()
        }
    }

    fun deletePaymentCard(paymentCardId: String) {
        paymentWalletRepository.deletePaymentCard(paymentCardId, deleteRequest, _deleteError)
    }


    fun fetchLocalData() {
        loyaltyWalletRepository.getLocalData(
            localMembershipPlanData,
            localMembershipCardData,
            _fetchError,
            loyaltyUpdateDone
        )
        paymentWalletRepository.getLocalData(paymentCards, _fetchError, paymentUpdateDone)
    }
}
