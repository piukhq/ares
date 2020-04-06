package com.bink.wallet.scenes.add_auth_enrol.view_models

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
import com.bink.wallet.model.response.membership_plan.PlanDocument
import com.bink.wallet.model.response.membership_plan.PlanField
import com.bink.wallet.model.response.payment_card.PaymentCard
import com.bink.wallet.scenes.add_auth_enrol.screens.BaseAddAuthFragment
import com.bink.wallet.scenes.loyalty_wallet.LoyaltyWalletRepository
import com.bink.wallet.utils.EMPTY_STRING
import com.bink.wallet.utils.enums.FieldType
import com.bink.wallet.utils.enums.TypeOfField

open class AddAuthViewModel constructor(private val loyaltyWalletRepository: LoyaltyWalletRepository) :
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

    val planFieldsList: MutableList<Pair<Any, PlanFieldsRequest>> =
        mutableListOf()
    val planDocumentsList: MutableList<Pair<Any, PlanFieldsRequest>> =
        mutableListOf()

    val addRegisterFieldsRequest = MutableLiveData<Account>()


    init {
        _paymentCardsMerger.addSource(paymentCards) {
            _paymentCardsMerger.value = paymentCards.value
        }
        _paymentCardsMerger.addSource(localPaymentCards) {
            _paymentCardsMerger.value = localPaymentCards.value
        }
    }

    fun addPlanField(planField: PlanField) {
        val pairPlanField = Pair(
            planField, PlanFieldsRequest(
                planField.column, EMPTY_STRING
            )
        )
        if (planField.type == FieldType.BOOLEAN_OPTIONAL.type) {
            planDocumentsList.add(
                pairPlanField
            )
        } else if (!planField.column.equals(BaseAddAuthFragment.BARCODE_TEXT)) {
            planFieldsList.add(
                pairPlanField
            )
        }
    }

    open fun addItems() {}

    fun addPlanDocument(planDocument: PlanDocument) {
        planDocumentsList.add(
            Pair(
                planDocument, PlanFieldsRequest(
                    planDocument.name, EMPTY_STRING
                )
            )
        )
    }

    fun mapItems() {
        planDocumentsList.map { planFieldsList.add(it) }
        val addRegisterFieldsRequest = Account()

        planFieldsList.map {
            if (it.first is PlanField) {
                when ((it.first as PlanField).typeOfField) {
                    TypeOfField.ADD -> addRegisterFieldsRequest.add_fields?.add(it.second)
                    TypeOfField.AUTH -> addRegisterFieldsRequest.authorise_fields?.add(it.second)
                    TypeOfField.ENROL -> addRegisterFieldsRequest.enrol_fields?.add(it.second)
                    else -> addRegisterFieldsRequest.registration_fields?.add(it.second)
                }
            } else
                addRegisterFieldsRequest.plan_documents?.add(it.second)
        }
        this.addRegisterFieldsRequest.value = addRegisterFieldsRequest
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
