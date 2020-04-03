package com.bink.wallet.scenes.add_auth_enrol

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.bink.wallet.BaseViewModel
import com.bink.wallet.model.request.membership_card.Account
import com.bink.wallet.model.request.membership_card.MembershipCardRequest
import com.bink.wallet.model.request.membership_card.PlanFieldsRequest
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.model.response.membership_plan.PlanField
import com.bink.wallet.model.response.payment_card.PaymentCard
import com.bink.wallet.scenes.loyalty_wallet.LoyaltyWalletRepository
import com.bink.wallet.utils.enums.TypeOfField

class AddAuthViewModel constructor(private val loyaltyWalletRepository: LoyaltyWalletRepository) :
    BaseViewModel() {

    val newMembershipCard = MutableLiveData<MembershipCard>()
    val createCardError = MutableLiveData<Exception>()
    val paymentCards = MutableLiveData<List<PaymentCard>>()
    private val _localPaymentCards = MutableLiveData<List<PaymentCard>>()
    val localPaymentCards: LiveData<List<PaymentCard>>
        get() = _localPaymentCards
    private val _fetchCardsError = MutableLiveData<Exception>()
    val fetchCardsError: LiveData<Exception>
        get() = _fetchCardsError
    private val _fetchLocalCardsError = MutableLiveData<Exception>()
    val fetchLocalCardsError: LiveData<Exception>
        get() = _fetchLocalCardsError

    private val _paymentCardsMerger = MediatorLiveData<List<PaymentCard>>()
    val paymentCardsMerger: LiveData<List<PaymentCard>>
        get() = _paymentCardsMerger

    val currentMembershipPlan = MutableLiveData<MembershipPlan>()

    val ctaText = ObservableField<String>()
    val titleText = ObservableField<String>()
    val descriptionText = ObservableField<String>()
    val isNoAccountFooter = ObservableBoolean(false)
    val haveValidationsPassed = ObservableBoolean(false)
    val isKeyboardHidden = ObservableBoolean(true)


    init {
        _paymentCardsMerger.addSource(paymentCards) {
            _paymentCardsMerger.value = paymentCards.value
        }
        _paymentCardsMerger.addSource(localPaymentCards) {
            _paymentCardsMerger.value = localPaymentCards.value
        }
    }

    fun createMembershipCard(membershipCardRequest: MembershipCardRequest) {
        loyaltyWalletRepository.createMembershipCard(
            membershipCardRequest,
            newMembershipCard,
            createCardError
        )
    }

    fun updateMembershipCard(
        membershipCardId: String,
        membershipCardRequest: MembershipCardRequest
    ) {
        loyaltyWalletRepository.updateMembershipCard(
            membershipCardId,
            membershipCardRequest,
            newMembershipCard,
            createCardError
        )
    }

    fun ghostMembershipCard(
        membershipCardId: String,
        membershipCardRequest: MembershipCardRequest
    ) {
        loyaltyWalletRepository.ghostMembershipCard(
            membershipCardId,
            membershipCardRequest,
            newMembershipCard,
            createCardError
        )
    }

    fun getPaymentCards() {
        loyaltyWalletRepository.getPaymentCards(paymentCards, _fetchCardsError)
    }

    fun getLocalPaymentCards() {
        loyaltyWalletRepository.getLocalPaymentCards(_localPaymentCards, _fetchLocalCardsError)
    }
}
