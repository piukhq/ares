package com.bink.wallet.scenes.add_auth_enrol.adapter

import androidx.appcompat.widget.AppCompatEditText
import androidx.databinding.ViewDataBinding
import com.bink.wallet.R
import com.bink.wallet.model.request.membership_card.PlanFieldsRequest
import com.bink.wallet.model.response.membership_plan.PlanField
import com.bink.wallet.scenes.BaseViewHolder
import com.bink.wallet.utils.EMPTY_STRING
import com.bink.wallet.utils.UtilFunctions
import com.bink.wallet.utils.enums.FieldType

abstract class BaseAddAuthViewHolder<T>(
    viewDataBinding: ViewDataBinding
) :
    BaseViewHolder<T>(viewDataBinding) {

    var finalTextField: String = EMPTY_STRING
    var brands = mutableListOf<Pair<Any, PlanFieldsRequest>>()
    var buttonRefresh: () -> Unit = {}
    var item = null

    init {
        brands.map { pair ->
            if (pair.first is PlanField &&
                (pair.first as PlanField).type == FieldType.TEXT.type
            ) {
                (pair.first as PlanField).column?.let { column ->
                    finalTextField = column
                }
            }
        }
    }

    override fun bind(item: T) {

    }

    fun checkIfError(position: Int, text: AppCompatEditText) {
        val currentItem = brands[position]
        if (!UtilFunctions.isValidField(
                (currentItem.first as PlanField).validation,
                currentItem.second.value
            )
        ) {
            text.error = text.context.getString(
                R.string.add_auth_error_message,
                (currentItem.first as PlanField).column
            )
        }
    }

}