package com.bink.wallet.scenes.pll

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.bink.wallet.BaseViewModel
import com.bink.wallet.R
import com.bink.wallet.data.SharedPreferenceManager
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.model.response.payment_card.PaymentCard
import com.bink.wallet.utils.DateTimeUtils
import com.bink.wallet.utils.SentryErrorType
import com.bink.wallet.utils.SentryUtils
import okhttp3.ResponseBody
import retrofit2.HttpException

class PllViewModel(private val paymentWalletRepository: PaymentWalletRepository) : BaseViewModel() {
    val membershipCard = MutableLiveData<MembershipCard>()
    val membershipPlan = MutableLiveData<MembershipPlan>()
    val unlinkedRequestBody = MutableLiveData<ResponseBody>()
    val linkError = MutableLiveData<Exception>()
    val unlinkError = MutableLiveData<Exception>()
    val fetchError = MutableLiveData<Exception>()
    val localFetchError = MutableLiveData<Exception>()

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

    private val _unlinkErrors = MutableLiveData<ArrayList<Exception>>()
    val unlinkErrors: LiveData<ArrayList<Exception>>
        get() = _unlinkErrors

    val unlinkSuccesses: LiveData<ArrayList<Any>>
        get() = _unlinkSuccesses
    private val _unlinkSuccesses = MutableLiveData<ArrayList<Any>>()

    val linkErrors: LiveData<MutableList<Exception>>
        get() = _linkErrors
    private val _linkErrors = MutableLiveData<MutableList<Exception>>()

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
        val shouldMakePeriodicCall =
            DateTimeUtils.haveTwoMinutesElapsed(SharedPreferenceManager.paymentCardsLastRequestTime)

        if (shouldMakePeriodicCall) {
            paymentWalletRepository.getPaymentCards(
                _paymentCards,
                fetchError
            )
        } else {
            paymentWalletRepository.getLocalPaymentCards(_paymentCards, fetchError)
        }
    }

    fun getLocalPaymentCards() {
        paymentWalletRepository.getLocalPaymentCards(
            _localPaymentCards,
            localFetchError
        )
    }

    fun unlinkPaymentCards(paymentCards: List<PaymentCard>, membershipCard: MembershipCard) {
        paymentWalletRepository.unlinkPaymentCards(
            paymentCards,
            membershipCard,
            _unlinkSuccesses,
            _unlinkErrors
        )
    }

    fun linkPaymentCards(paymentCards: List<PaymentCard>, membershipCard: MembershipCard) {
        paymentWalletRepository.linkPaymentCards(
            paymentCards,
            membershipCard,
            _linkSuccesses,
            _linkErrors
        )
    }

    fun hasDisplayableError(context: Context, exceptions: ArrayList<Exception>): String? {
        for (exception in exceptions) {
            val errorResponse = (exception as HttpException).response()
            val errorCode = errorResponse?.code()
            val errorBody = errorResponse?.errorBody()?.string()

            if (errorCode == 403) {
                return errorBody
            }

            if (errorCode == 404) {
                SentryUtils.logError(SentryErrorType.LOYALTY_API_REJECTED, exception)
                return context.getString(R.string.pll_404_error)
            }
        }
        return null
    }
}