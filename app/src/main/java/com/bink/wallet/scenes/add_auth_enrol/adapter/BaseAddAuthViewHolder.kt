package com.bink.wallet.scenes.add_auth_enrol.adapter

import androidx.appcompat.widget.AppCompatEditText
import androidx.databinding.ViewDataBinding
import com.bink.wallet.R
import com.bink.wallet.model.response.membership_plan.PlanField
import com.bink.wallet.scenes.BaseViewHolder
import com.bink.wallet.scenes.add_auth_enrol.AddAuthItemWrapper
import com.bink.wallet.utils.EMPTY_STRING
import com.bink.wallet.utils.UtilFunctions
import com.bink.wallet.utils.enums.AddAuthItemType
import com.bink.wallet.utils.enums.FieldType

abstract class BaseAddAuthViewHolder<T>(
    viewDataBinding: ViewDataBinding
) :
    BaseViewHolder<T>(viewDataBinding) {

    var addAuthItems = mutableListOf<AddAuthItemWrapper>()
    var checkValidation: () -> Unit = {}

    override fun bind(item: T) {}

    fun checkIfError(position: Int, text: AppCompatEditText) {
        val currentItem = addAuthItems[position]
        if (currentItem.getFieldType() == AddAuthItemType.PLAN_FIELD) {
            val currentPlanField = currentItem.fieldType as PlanField
            val requestValue = currentItem.fieldsRequest.value
            if (!UtilFunctions.isValidField(
                    currentPlanField.validation,
                    requestValue
                )
            ) {
                text.error = text.context.getString(
                    R.string.add_auth_error_message,
                    currentPlanField.column
                )
            }
        }
    }

}