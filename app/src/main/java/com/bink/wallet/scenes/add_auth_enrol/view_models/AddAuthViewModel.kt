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
import com.bink.wallet.scenes.add_auth_enrol.AddAuthItemWrapper
import com.bink.wallet.scenes.loyalty_wallet.LoyaltyWalletRepository
import com.bink.wallet.utils.EMPTY_STRING
import com.bink.wallet.utils.UtilFunctions
import com.bink.wallet.utils.enums.AddAuthItemType
import com.bink.wallet.utils.enums.FieldType
import com.bink.wallet.utils.enums.SignUpFieldTypes
import com.bink.wallet.utils.enums.TypeOfField

open class AddAuthViewModel constructor(private val loyaltyWalletRepository: LoyaltyWalletRepository) :
    BaseViewModel() {

    val ctaText = ObservableField<String>()
    val titleText = ObservableField<String>()
    val descriptionText = ObservableField<String>()
    val isNoAccountFooter = ObservableBoolean(false)
    val haveValidationsPassed = ObservableBoolean(false)
    val isKeyboardHidden = ObservableBoolean(true)

    val addAuthItemsList: MutableList<AddAuthItemWrapper> = mutableListOf()

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
        val addAuthItemWrapper =
            AddAuthItemWrapper(planField, PlanFieldsRequest(planField.column, EMPTY_STRING))
        addAuthItemsList.add(addAuthItemWrapper)
    }

    fun addPlanDocument(planDocument: PlanDocument) {
        addAuthItemsList.add(
            AddAuthItemWrapper(
                planDocument,
                PlanFieldsRequest(planDocument.name, EMPTY_STRING)
            )
        )
    }

    private fun addHeader() {
        addAuthItemsList.add(AddAuthItemWrapper(FieldType.HEADER))
    }

    open fun addItems(membershipPlan: MembershipPlan) {
        addHeader()
    }

    fun mapItems() {
        val addRegisterFieldsRequest = Account()
        addAuthItemsList.forEach { addAuthItem ->
            addAuthItem.fieldsRequest?.let {
                if (addAuthItem.getFieldType() == AddAuthItemType.PLAN_FIELD) {
                    when ((addAuthItem.fieldType as PlanField).typeOfField) {
                        TypeOfField.ADD -> addRegisterFieldsRequest.add_fields?.add(addAuthItem.fieldsRequest)
                        TypeOfField.AUTH -> addRegisterFieldsRequest.authorise_fields?.add(
                            addAuthItem.fieldsRequest
                        )
                        TypeOfField.ENROL -> addRegisterFieldsRequest.enrol_fields?.add(addAuthItem.fieldsRequest)
                        else -> addRegisterFieldsRequest.registration_fields?.add(addAuthItem.fieldsRequest)
                    }
                } else {
                    addRegisterFieldsRequest.plan_documents?.add(addAuthItem.fieldsRequest)
                }
            }
        }
        arrangeAuthItems()
        _addRegisterFieldsRequest.value = addRegisterFieldsRequest
    }

    private fun arrangeAuthItems() {
        val planDocuments =
            addAuthItemsList.filter { item ->
                item.getFieldType() == AddAuthItemType.PLAN_DOCUMENT ||
                        item.getFieldType() == AddAuthItemType.PLAN_FIELD && (item.fieldType as PlanField).isBooleanType()
            }
        val planFields =
            addAuthItemsList.filter { item ->
                item.getFieldType() == AddAuthItemType.PLAN_FIELD && !(item.fieldType as PlanField).isBooleanType()
            }
        val header =
            addAuthItemsList.filter { item -> item.getFieldType() == AddAuthItemType.HEADER }
        addAuthItemsList.clear()
        addAuthItemsList.add(header[0])
        planFields.forEach { item -> addAuthItemsList.add(item) }
        planDocuments.forEach { item -> addAuthItemsList.add(item) }
    }

    fun didPlanDocumentsPassValidations(addRegisterFieldsRequest: Account): Boolean {
        addRegisterFieldsRequest.plan_documents?.map { planDocument ->
            var required = true
            addAuthItemsList.filter { addAuthItem ->
                addAuthItem.getFieldType() == AddAuthItemType.PLAN_DOCUMENT
            }.map { addAuthItem ->
                if (addAuthItem.fieldsRequest?.column == planDocument.column) {
                    (addAuthItem.fieldType as PlanDocument).checkbox?.let { bool ->
                        required = bool
                    }
                    if (required &&
                        planDocument.value != true.toString()
                    ) {
                        return false
                    }
                }
            }
        }
        return true
    }

    fun didPlanFieldsPassValidations(): Boolean {
        addAuthItemsList.filter { item -> item.getFieldType() == AddAuthItemType.PLAN_FIELD }
            .map { addAuthItem ->
                val item = addAuthItem.fieldType as PlanField
                if (item.type != FieldType.BOOLEAN_OPTIONAL.type) {
                    if (addAuthItem.fieldsRequest?.value.isNullOrEmpty()) {
                        return false
                    } else if (!UtilFunctions.isValidField(
                            item.validation,
                            addAuthItem.fieldsRequest?.value
                        )
                    ) {

                        return false
                    }
                }
            }
        return true
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

    fun setBarcode(barcode: String) {
        var itemToRemove: AddAuthItemWrapper? = null
        addAuthItemsList.forEach { addAuthItem ->
            if (addAuthItem.fieldType is PlanField) {
                if ((addAuthItem.fieldType).common_name == SignUpFieldTypes.BARCODE.common_name) {
                    addAuthItem.fieldsRequest?.value = barcode
                    addAuthItem.fieldsRequest?.disabled = true
                }

                if ((addAuthItem.fieldType).common_name == SignUpFieldTypes.CARD_NUMBER.common_name) {
                    itemToRemove = addAuthItem
                }
            }
        }

        itemToRemove?.let { safeWrapper ->
            addAuthItemsList.remove(safeWrapper)
        }
    }
}
