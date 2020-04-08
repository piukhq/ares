package com.bink.wallet.scenes.add_auth_enrol.adapter

import CheckboxViewHolder
import DisplayViewHolder
import SpinnerViewHolder
import TextFieldViewHolder
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.databinding.*
import com.bink.wallet.model.response.membership_plan.PlanDocument
import com.bink.wallet.model.response.membership_plan.PlanField
import com.bink.wallet.scenes.add_auth_enrol.AddAuthItemWrapper
import com.bink.wallet.utils.enums.AddAuthItemType
import com.bink.wallet.utils.enums.FieldType


@Suppress("UNCHECKED_CAST")
class AddAuthAdapter(
    private val addAuthItems: MutableList<AddAuthItemWrapper>,
    val checkValidation: () -> Unit = {}
) :
    RecyclerView.Adapter<BaseAddAuthViewHolder<*>>() {
    override fun onBindViewHolder(holder: BaseAddAuthViewHolder<*>, position: Int) {
        holder.addAuthItems = addAuthItems
        holder.checkValidation = checkValidation

        addAuthItems[position].let { addAuthItem ->
            if (addAuthItem.getFieldType() == AddAuthItemType.PLAN_FIELD) {
                when (holder) {
                    is TextFieldViewHolder -> holder.bind(addAuthItem)
                    is SpinnerViewHolder -> holder.bind(addAuthItem)
                    is CheckboxViewHolder -> holder.bind(addAuthItem)
                    is DisplayViewHolder -> holder.bind(addAuthItem)
                }
            } else {
                if (getItemViewType(position) == FieldType.DISPLAY.type) {
                    (holder as DisplayViewHolder).bind(addAuthItem)
                } else {
                    (holder as CheckboxViewHolder).bind(addAuthItem)
                }
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseAddAuthViewHolder<*> {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            FieldType.TEXT.type,
            FieldType.PASSWORD.type -> TextFieldViewHolder(
                AddAuthTextItemBinding.inflate(inflater)
            )
            FieldType.SPINNER.type -> SpinnerViewHolder(
                AddAuthSpinnerItemBinding.inflate(inflater)
            )
            FieldType.DISPLAY.type -> DisplayViewHolder(
                AddAuthDisplayItemBinding.inflate(inflater)
            )
            else -> CheckboxViewHolder(AddAuthCheckboxItemBinding.inflate(inflater))
        }
    }

    override fun getItemViewType(position: Int): Int {
        with(addAuthItems[position]) {
            if (getFieldType() == AddAuthItemType.PLAN_FIELD) {
                (fieldType as PlanField).type?.let {
                    return it
                }
            } else {
                val checkBox = (fieldType as PlanDocument).checkbox
                return if (checkBox == null || checkBox) {
                    FieldType.BOOLEAN_REQUIRED.type
                } else {
                    FieldType.DISPLAY.type
                }
            }
        }
        return 0
    }

    override fun getItemCount() = addAuthItems.size

    companion object {
        const val COMMON_NAME_EMAIL = "email"
    }
}