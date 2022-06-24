package com.bink.wallet.scenes.payment_card_details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bink.wallet.BaseViewModel
import com.bink.wallet.model.auth.User
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.model.response.payment_card.PaymentCard
import com.bink.wallet.scenes.loyalty_wallet.wallet.LoyaltyWalletRepository
import com.bink.wallet.scenes.pll.PaymentWalletRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody

class PaymentCardsDetailsViewModel(
    private var paymentWalletRepository: PaymentWalletRepository,
    private var loyaltyWalletRepository: LoyaltyWalletRepository
) :
    BaseViewModel() {

    val paymentCard = MutableLiveData<PaymentCard>()
    val membershipCardData: MutableLiveData<List<MembershipCard>> = MutableLiveData()
    val membershipPlanData: MutableLiveData<List<MembershipPlan>> = MutableLiveData()
    val deleteRequest = MutableLiveData<ResponseBody>()

    private val _loadCardsError = MutableLiveData<Exception>()

    private val _linkError = MutableLiveData<Pair<Exception, String>>()
    val linkError: LiveData<Pair<Exception, String>>
        get() = _linkError

    private val _unlinkError = MutableLiveData<Exception>()
    val unlinkError: LiveData<Exception>
        get() = _unlinkError

    private var _deleteError = MutableLiveData<Exception>()
    val deleteError: LiveData<Exception>
        get() = _deleteError
    private val _getCardError = MutableLiveData<Exception>()

    val getCardError: LiveData<Exception>
        get() = _getCardError

    private val _userResponse = MutableLiveData<User>()
    val userResponse: LiveData<User>
        get() = _userResponse


    fun linkPaymentCard(cardId: String, membershipPlanId: String) {
        val membershipCard = membershipCardData.value?.firstOrNull { card -> card.id == cardId }
        updatePaymentCard(cardId)
        membershipCard?.let { mCard ->
            paymentCard.value?.let { pCard ->
                paymentWalletRepository.linkPaymentCard(
                    mCard,
                    pCard,
                    _linkError,
                    paymentCard,
                    membershipPlanId
                )
            }

        }
    }

    fun unlinkPaymentCard(cardId: String) {
        val membershipCard = membershipCardData.value?.firstOrNull { card -> card.id == cardId }
        membershipCard?.let { mCard ->
            paymentCard.value?.let { pCard ->
                paymentWalletRepository.unlinkPaymentCard(
                    pCard,
                    mCard,
                    _unlinkError,
                    paymentCard
                )
            }

        }

    }

    fun getPaymentCard(id: Int) {
        viewModelScope.launch {
            try {
                val card =
                    withContext(Dispatchers.IO) { paymentWalletRepository.getPaymentCard(id.toString()) }
                paymentCard.value = card
            } catch (e: Exception) {
                _getCardError.value = e
            }
        }
    }

    fun deletePaymentCard(paymentCardId: String) {
        paymentWalletRepository.deletePaymentCard(paymentCardId, deleteRequest, _deleteError)
    }

    fun getMembershipCards() {
        loyaltyWalletRepository.retrieveMembershipCards(membershipCardData, _loadCardsError)
    }

    fun storePaymentCard(card: PaymentCard) {
        paymentWalletRepository.storePaymentCard(card)
    }

    private fun updatePaymentCard(cardId: String) {
        paymentCard.value?.let {
            if (it.membership_cards.isEmpty()) {
                it.addPaymentCard(cardId)
            }
        }
    }

}
