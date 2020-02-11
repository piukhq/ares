package com.bink.wallet.scenes.pll

import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.bink.wallet.BaseViewModel
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.model.response.payment_card.PaymentCard
import okhttp3.ResponseBody

class PllViewModel(private val paymentWalletRepository: PaymentWalletRepository) : BaseViewModel() {
    val membershipCard = MutableLiveData<MembershipCard>()
    val membershipPlan = MutableLiveData<MembershipPlan>()
    val title = ObservableField<String>()
    val unlinkedRequestBody = MutableLiveData<ResponseBody>()
    val linkError = MutableLiveData<Throwable>()
    val unlinkError = MutableLiveData<Throwable>()
    val fetchError = MutableLiveData<Throwable>()
    val localFetchError = MutableLiveData<Throwable>()

    val paymentCard = MutableLiveData<PaymentCard>()

    private val _paymentCards = MutableLiveData<List<PaymentCard>>()
    val paymentCards: LiveData<List<PaymentCard>>
        get() = _paymentCards

    private val _localPaymentCards = MutableLiveData<List<PaymentCard>>()
    val localPaymentCards: LiveData<List<PaymentCard>>
        get() = _localPaymentCards

    private val _paymentCardsMerger = MediatorLiveData<List<PaymentCard>>()
    val paymentCardsMerger: LiveData<List<PaymentCard>>
        get() = _paymentCardsMerger
    val unlinkErrors = MutableLiveData<ArrayList<Throwable>>()
    val unlinkSuccesses = MutableLiveData<ArrayList<Any>>()

    val linkErrors = MutableLiveData<MutableList<Throwable>>()
    val linkSuccesses = MutableLiveData<ArrayList<Any>>()

    init {
        _paymentCardsMerger.addSource(paymentCards) {
            _paymentCardsMerger.value = paymentCards.value
        }
        _paymentCardsMerger.addSource(localPaymentCards) {
            _paymentCardsMerger.value = localPaymentCards.value
        }
    }

    fun getPaymentCards() {
        paymentWalletRepository.getPaymentCards(
            _paymentCards,
            fetchError
        )
    }

    fun getLocalPaymentCards() {
        paymentWalletRepository.getLocalPaymentCards(
            _localPaymentCards,
            localFetchError
        )
    }

    fun unlinkPaymentCards(paymentCardIds: List<String>, membershipCardId: String) {
        paymentWalletRepository.unlinkPaymentCards(
            paymentCardIds,
            membershipCardId,
            unlinkSuccesses,
            unlinkErrors
        )
    }

    fun linkPaymentCards(paymentCardIds: List<String>, membershipCardId: String) {
        paymentWalletRepository.linkPaymentCards(
            paymentCardIds,
            membershipCardId,
            linkSuccesses,
            linkErrors
        )
    }
}