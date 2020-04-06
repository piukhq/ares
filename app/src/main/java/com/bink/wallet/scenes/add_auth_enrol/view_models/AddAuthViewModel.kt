package com.bink.wallet.scenes.add_auth_enrol.view_models

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bink.wallet.BaseViewModel
import com.bink.wallet.model.request.membership_card.Account
import com.bink.wallet.model.request.membership_card.MembershipCardRequest
import com.bink.wallet.model.request.membership_card.PlanFieldsRequest
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.model.response.membership_plan.PlanDocument
import com.bink.wallet.model.response.membership_plan.PlanField
import com.bink.wallet.scenes.add_auth_enrol.screens.BaseAddAuthFragment
import com.bink.wallet.scenes.loyalty_wallet.LoyaltyWalletRepository
import com.bink.wallet.utils.EMPTY_STRING
import com.bink.wallet.utils.enums.FieldType
import com.bink.wallet.utils.enums.TypeOfField

open class AddAuthViewModel constructor(private val loyaltyWalletRepository: LoyaltyWalletRepository) :
    BaseViewModel() {

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

    private val _addRegisterFieldsRequest = MutableLiveData<Account>()
    val addRegisterFieldsRequest: LiveData<Account>
        get() = _addRegisterFieldsRequest
    private val _newMembershipCard = MutableLiveData<MembershipCard>()
    val newMembershipCard: LiveData<MembershipCard>
        get() = _newMembershipCard
    private val _createCardError = MutableLiveData<Exception>()
    val createCardError: LiveData<Exception>
        get() = _createCardError


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

    open fun addItems(membershipPlan: MembershipPlan) {}

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
        _addRegisterFieldsRequest.value = addRegisterFieldsRequest
    }

    fun createMembershipCard(membershipCardRequest: MembershipCardRequest) {
        loyaltyWalletRepository.createMembershipCard(
            membershipCardRequest,
            _newMembershipCard,
            _createCardError
        )
    }

    fun updateMembershipCard(
        membershipCardId: String,
        membershipCardRequest: MembershipCardRequest
    ) {
        loyaltyWalletRepository.updateMembershipCard(
            membershipCardId,
            membershipCardRequest,
            _newMembershipCard,
            _createCardError
        )
    }

    fun ghostMembershipCard(
        membershipCardId: String,
        membershipCardRequest: MembershipCardRequest
    ) {
        loyaltyWalletRepository.ghostMembershipCard(
            membershipCardId,
            membershipCardRequest,
            _newMembershipCard,
            _createCardError
        )
    }
}
