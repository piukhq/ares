package com.bink.wallet.scenes.add_auth_enrol

import com.bink.wallet.model.request.membership_card.Account
import com.bink.wallet.model.request.membership_card.PlanFieldsRequest
import com.bink.wallet.model.response.membership_plan.PlanField
import com.bink.wallet.utils.UtilFunctions
import com.bink.wallet.utils.enums.FieldType
import com.bink.wallet.utils.enums.TypeOfField

object FormsUtil {

    private var fields = mutableMapOf<Int, FormField>()
    private var planDocuments = mutableMapOf<Int, Boolean>()

    //Represents an individual formField e.g First name
    fun addFormField(position: Int, planField: PlanField) {
        val isSensitive = planField.type == FieldType.SENSITIVE.type
        fields[position] = FormField(planField, PlanFieldsRequest(planField.column, null, null,isSensitive = isSensitive))
    }

    fun getSize() = fields.size

    fun clearForms() {
        fields.clear()
        planDocuments.clear()
    }

    fun updateField(position: Int, value: String) {
        val form = fields[position]

        form?.fieldsRequest?.value = value

        form?.isValidField = if (value.isEmpty()) false else UtilFunctions.isValidField(form?.planField?.validation,value)
    }

    fun updateValidation(position: Int, isValid: Boolean) {
        fields[position]?.isValidField = isValid
    }

    fun returnForms() = fields

    fun returnPlanDocument() = planDocuments

    fun areAllFieldsValid(): Boolean {
        return areAllFormFieldsValid() && areAllPlanDocumentsValid()
    }

    fun addPlanDocument(position: Int, hasBeenTicked: Boolean) {
        planDocuments[position] = hasBeenTicked
    }

    fun getAccount(): Account {
        val account = Account(
            mutableListOf(),
            mutableListOf(),
            mutableListOf(),
            mutableListOf()
        )

        fields.forEach { field ->
            val typeOfField = field.value.planField.typeOfField
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