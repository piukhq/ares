package com.bink.wallet.scenes.add_auth_enrol

import com.bink.wallet.model.request.membership_card.Account
import com.bink.wallet.model.request.membership_card.MembershipCardRequest
import com.bink.wallet.model.request.membership_card.PlanFieldsRequest
import com.bink.wallet.model.response.membership_plan.PlanField
import com.bink.wallet.utils.LocalStoreUtils
import com.bink.wallet.utils.REMEMBER_DETAILS_COMMON_NAME
import com.bink.wallet.utils.UtilFunctions
import com.bink.wallet.utils.enums.FieldType
import com.bink.wallet.utils.enums.TypeOfField
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.util.*

object FormsUtil {

    private var fields = mutableMapOf<Int, FormField>()
    private var planDocuments = mutableMapOf<Int, Boolean>()

    //Represents an individual formField e.g First name
    fun addFormField(position: Int, planField: PlanField) {
        val isSensitive = planField.type == FieldType.SENSITIVE.type
        fields[position] = FormField(
            planField,
            PlanFieldsRequest(planField.column, null, null, isSensitive = isSensitive, common_name = planField.common_name)
        )
    }


    fun clearForms() {
        fields.clear()
        planDocuments.clear()
    }

    fun updateField(position: Int, value: String) {
        val form = fields[position]

        form?.fieldsRequest?.value = value

        form?.isValidField = if (value.isEmpty()) false else UtilFunctions.isValidField(
            form?.planField?.validation,
            value
        )
    }

    fun updateValidation(position: Int, isValid: Boolean) {
        fields[position]?.isValidField = isValid
    }

    fun areAllFieldsValid(): Boolean {
        return areAllFormFieldsValid() && areAllPlanDocumentsValid()
    }

    fun addPlanDocument(position: Int, hasBeenTicked: Boolean) {
        planDocuments[position] = hasBeenTicked
    }

    fun fieldHasNotBeenAdded(position: Int?): Boolean {
        return fields[position] == null
    }

    //The request object which will be sent in api call
    fun getAccount(): Account {
        val account = Account(
            mutableListOf(),
            mutableListOf(),
            mutableListOf(),
            mutableListOf()
        )

        fields.forEach { field ->
            val typeOfField = field.value.planField.typeOfField
            field.value.planField.common_name
            field.value.fieldsRequest?.isSensitive =
                field.value.planField.type == FieldType.SENSITIVE.type

            val planRequest = field.value.fieldsRequest

            when (typeOfField) {
                TypeOfField.ADD -> planRequest?.let {
                    account.add_fields?.add(
                        it
                    )
                }

                TypeOfField.AUTH -> planRequest?.let {
                    account.authorise_fields?.add(
                        it
                    )
                }

                TypeOfField.ENROL -> planRequest?.let {
                    account.enrol_fields?.add(
                        it
                    )
                }

                else -> planRequest?.let {
                    account.registration_fields?.add(
                        it
                    )
                }
            }
        }

        return account
    }

    fun getFormField(position: Int): FormField? {
        return fields[position]
    }

    fun saveFormField(commonName: String?, value: String?) {
        var existingData = ArrayList<String>()

        commonName?.let { fieldName ->
            getFormFields(fieldName)?.let {
                existingData = it
            }
        }

        value?.let { fieldValue ->
            if (!existingData.contains(fieldValue)) {
                existingData.add(fieldValue)
            }
        }

        LocalStoreUtils.getAppSharedPref(LocalStoreUtils.KEY_EMAIL)?.let {
            existingData.remove(it)
        }

        commonName?.let { LocalStoreUtils.setAppSharedPref(it, Gson().toJson(existingData)) }
    }

    fun stripRememberDetailsField(request: MembershipCardRequest): MembershipCardRequest {
        request.account?.registration_fields!!.removeAll { it.common_name == REMEMBER_DETAILS_COMMON_NAME }
        return request
    }

    fun getFormFields(commonName: String): ArrayList<String>? {
        var fields: ArrayList<String>? = null

        if (commonName == "email") {
            LocalStoreUtils.getAppSharedPref(LocalStoreUtils.KEY_EMAIL)?.let { loggedInEmail ->
                fields = arrayListOf(loggedInEmail)
            }
        }

        LocalStoreUtils.getAppSharedPref(commonName)?.let { formFieldsAsString ->
            val gson = GsonBuilder().create()
            if (fields == null) fields = ArrayList<String>()
            fields?.addAll(gson.fromJson(formFieldsAsString, object : TypeToken<ArrayList<String>>() {}.type))
            return fields
        }

        return fields
    }

    private fun areAllFormFieldsValid(): Boolean {
        fields.forEach { field ->
            if (!field.value.isValidField) {
                return false
            }
        }

        return true
    }

    private fun areAllPlanDocumentsValid(): Boolean {
        planDocuments.forEach { planDocument ->
            if (!planDocument.value) {
                return false
            }
        }

        return true
    }

}