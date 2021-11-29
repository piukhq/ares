package com.bink.wallet.scenes.add_auth_enrol.view_models

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bink.wallet.BaseViewModel
import com.bink.wallet.model.currentAgent
import com.bink.wallet.model.getRemoteAuthFields
import com.bink.wallet.model.isEnabled
import com.bink.wallet.model.request.membership_card.Account
import com.bink.wallet.model.request.membership_card.MembershipCardRequest
import com.bink.wallet.model.request.membership_card.PlanFieldsRequest
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.model.response.membership_plan.PlanDocument
import com.bink.wallet.model.response.membership_plan.PlanField
import com.bink.wallet.scenes.add_auth_enrol.AddAuthItemWrapper
import com.bink.wallet.scenes.add_auth_enrol.FormsUtil
import com.bink.wallet.scenes.login.LoginRepository
import com.bink.wallet.scenes.loyalty_wallet.wallet.LoyaltyWalletRepository
import com.bink.wallet.utils.*
import com.bink.wallet.utils.enums.AddAuthItemType
import com.bink.wallet.utils.enums.FieldType
import com.bink.wallet.utils.enums.SignUpFieldTypes
import com.bink.wallet.utils.enums.TypeOfField
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.*

open class AddAuthViewModel constructor(private val loyaltyWalletRepository: LoyaltyWalletRepository, private var loginRepository: LoginRepository) :
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
    private val _addLoyaltyCardRequestMade = MutableLiveData<Boolean>()
    val addLoyaltyCardRequestMade: LiveData<Boolean>
        get() = _addLoyaltyCardRequestMade
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean>
        get() = _loading

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

    open fun addItems(membershipPlan: MembershipPlan, shouldExcludeBarcode: Boolean = true) {
        addHeader()
    }

    fun mapItems(membershipPlanId: String) {
        val addRegisterFieldsRequest = Account()

        addAuthItemsList.addAll(getWebScrapeFields(membershipPlanId))

        addAuthItemsList.forEach { addAuthItem ->
            addAuthItem.fieldsRequest?.let {
                if (addAuthItem.getFieldType() == AddAuthItemType.PLAN_FIELD) {
                    (addAuthItem.fieldType as PlanField)
                    it.isSensitive = addAuthItem.fieldType.type == FieldType.SENSITIVE.type

                    when (addAuthItem.fieldType.typeOfField) {
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

        getSaveCredentialsField { checkbox ->
            checkbox?.let {
                addAuthItemsList.add(it)
            }
            _addRegisterFieldsRequest.value = addRegisterFieldsRequest
        }

    }

    private fun getWebScrapeFields(membershipPlanId: String): List<AddAuthItemWrapper> {
        RemoteConfigUtil().localPointsCollection?.currentAgent(membershipPlanId.toIntOrNull())?.let { agent ->
            if (agent.isEnabled()) {
                return agent.fields.getRemoteAuthFields()
            }
        }

        return arrayListOf()
    }

    private fun getSaveCredentialsField(callback: (AddAuthItemWrapper?) -> Unit) {
        isRememberDetailsChecked { isChecked ->
            if (isChecked != null) {
                addAuthItemsList.forEach { item ->
                    if (item.getFieldType() == AddAuthItemType.PLAN_FIELD) {
                        if (REMEMBERABLE_FIELD_NAMES.contains((item.fieldType as PlanField).common_name?.toLowerCase(Locale.ENGLISH))) {
                            val planField = PlanField(REMEMBER_DETAILS_DISPLAY_NAME, null, REMEMBER_DETAILS_COMMON_NAME, 3, null, REMEMBER_DETAILS_DISPLAY_NAME, null, null)
                            callback(AddAuthItemWrapper(planField, PlanFieldsRequest(planField.column, isChecked.toString())))
                            return@isRememberDetailsChecked
                        }
                    }
                }
            }

            callback(null)
        }
    }

    fun checkDetailsToSave(membershipCardRequest: MembershipCardRequest) {
        val shouldSaveDetails = membershipCardRequest.account?.registration_fields?.filter { it.common_name == REMEMBER_DETAILS_COMMON_NAME }

        if (!shouldSaveDetails.isNullOrEmpty()) {
            if (shouldSaveDetails[0].value == true.toString()) {
                membershipCardRequest.account.add_fields?.forEach { addField ->
                    if (REMEMBERABLE_FIELD_NAMES.contains(addField.common_name?.toLowerCase(Locale.ENGLISH))) {
                        FormsUtil.saveFormField(addField.common_name?.toLowerCase(Locale.ENGLISH), addField.value)
                    }
                }

                membershipCardRequest.account.authorise_fields?.forEach { authField ->
                    if (REMEMBERABLE_FIELD_NAMES.contains(authField.common_name?.toLowerCase(Locale.ENGLISH))) {
                        FormsUtil.saveFormField(authField.common_name?.toLowerCase(Locale.ENGLISH), authField.value)
                    }
                }

                membershipCardRequest.account.enrol_fields?.forEach { enrolField ->
                    if (REMEMBERABLE_FIELD_NAMES.contains(enrolField.common_name?.toLowerCase(Locale.ENGLISH))) {
                        FormsUtil.saveFormField(enrolField.common_name?.toLowerCase(Locale.ENGLISH), enrolField.value)
                    }
                }

                membershipCardRequest.account.registration_fields?.forEach { registrationFields ->
                    if (REMEMBERABLE_FIELD_NAMES.contains(registrationFields.common_name?.toLowerCase(Locale.ENGLISH))) {
                        FormsUtil.saveFormField(registrationFields.common_name?.toLowerCase(Locale.ENGLISH), registrationFields.value)
                    }
                }
            }

            viewModelScope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        loginRepository.setPreference(
                            requestBody = JSONObject().put(
                                REMEMBER_DETAILS_KEY,
                                if (shouldSaveDetails[0].value == true.toString()) 1 else 0
                            ).toString()
                        )
                    }

                } catch (e: Exception) {

                }
            }

        }
    }

    private fun isRememberDetailsChecked(callback: (Boolean?) -> Unit) {
        viewModelScope.launch {
            try {
                val preferences = withContext(Dispatchers.IO) { loginRepository.getPreferences() }
                callback((preferences.filter { it.slug == REMEMBER_DETAILS_KEY }[0].value == "1"))
            } catch (e: Exception) {
                callback(null)
            }
        }
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

    fun updateScrapedCards(cards: List<MembershipCard>) {
        val scrapedCards = cards.filter { it.isScraped == true }
        for (card in scrapedCards) {
            loyaltyWalletRepository.storeMembershipCard(card)
        }
    }

    fun createMembershipCard(membershipCardRequest: MembershipCardRequest) {
        clearIgnoredFields(membershipCardRequest)
        loyaltyWalletRepository.createMembershipCard(
            FormsUtil.stripRememberDetailsField(membershipCardRequest),
            _newMembershipCard,
            _createCardError,
            _addLoyaltyCardRequestMade,
            _loading
        )
    }

    fun updateMembershipCard(
        membershipCardId: String,
        membershipCardRequest: MembershipCardRequest
    ) {

        loyaltyWalletRepository.updateMembershipCard(
            membershipCardId,
            FormsUtil.stripRememberDetailsField(membershipCardRequest),
            _newMembershipCard,
            _createCardError,
            _addLoyaltyCardRequestMade
        )
    }

    fun ghostMembershipCard(
        membershipCardId: String,
        membershipCardRequest: MembershipCardRequest
    ) {
        clearIgnoredFields(membershipCardRequest)
        loyaltyWalletRepository.ghostMembershipCard(
            membershipCardId,
            FormsUtil.stripRememberDetailsField(membershipCardRequest),
            _newMembershipCard,
            _createCardError,
            _addLoyaltyCardRequestMade
        )
    }

    fun setBarcode(barcode: String) {
        addAuthItemsList.forEach { addAuthItem ->
            if (addAuthItem.fieldType is PlanField) {
                if ((addAuthItem.fieldType).common_name == SignUpFieldTypes.BARCODE.common_name) {
                    addAuthItem.fieldsRequest?.value = barcode
                    addAuthItem.fieldsRequest?.disabled = true
                }

                if ((addAuthItem.fieldType).common_name == SignUpFieldTypes.CARD_NUMBER.common_name) {
                    addAuthItem.fieldsRequest?.shouldIgnore = true
                }
            }
        }
    }

    private fun clearIgnoredFields(cardRequest: MembershipCardRequest) {
        cardRequest.account?.add_fields?.let { list ->
            var planToRemove: PlanFieldsRequest? = null
            for (planField: PlanFieldsRequest in list) {
                if (planField.shouldIgnore) {
                    planToRemove = planField
                }
            }

            planToRemove?.let {
                list.remove(it)
            }
        }
    }
}
