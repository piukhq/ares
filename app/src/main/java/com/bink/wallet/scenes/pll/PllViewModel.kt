package com.bink.wallet.scenes.pll

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

    val _unlinkErrors = MutableLiveData<ArrayList<Throwable>>()
    val unlinkErrors: LiveData<ArrayList<Throwable>>
        get() = _unlinkErrors

    val unlinkSuccesses: LiveData<ArrayList<Any>>
        get() = _unlinkSuccesses
    private val _unlinkSuccesses = MutableLiveData<ArrayList<Any>>()

    val linkErrors: LiveData<MutableList<Throwable>>
        get() = _linkErrors
    private val _linkErrors = MutableLiveData<MutableList<Throwable>>()

    val linkSuccesses: LiveData<ArrayList<Any>>
        get() = _linkSuccesses
    private val _linkSuccesses = MutableLiveData<ArrayList<Any>>()

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
            _unlinkSuccesses,
            _unlinkErrors
        )
    }

    fun linkPaymentCards(paymentCardIds: List<String>, membershipCardId: String) {
        paymentWalletRepository.linkPaymentCards(
            paymentCardIds,
            membershipCardId,
            _linkSuccesses,
            _linkErrors
        )
    }
}