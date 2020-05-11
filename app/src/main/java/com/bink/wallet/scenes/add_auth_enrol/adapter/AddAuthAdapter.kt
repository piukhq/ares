package com.bink.wallet.scenes.add_auth_enrol.adapter

import DisplayViewHolder
import SpinnerViewHolder
import TextFieldViewHolder
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.databinding.AddAuthTextItemBinding
import com.bink.wallet.databinding.AddAuthSpinnerItemBinding
import com.bink.wallet.databinding.AddAuthDisplayItemBinding
import com.bink.wallet.databinding.AddAuthHeaderItemBinding
import com.bink.wallet.databinding.AddAuthCheckboxItemBinding
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.model.response.membership_plan.PlanDocument
import com.bink.wallet.model.response.membership_plan.PlanField
import com.bink.wallet.scenes.add_auth_enrol.AddAuthItemWrapper
import com.bink.wallet.utils.enums.AddAuthItemType
import com.bink.wallet.utils.enums.FieldType


@Suppress("UNCHECKED_CAST")
class AddAuthAdapter(
    private val addAuthItems: MutableList<AddAuthItemWrapper>,
    val membershipPlan: MembershipPlan?,
    private val headerTitle: String?,
    private val headerDescription: String?,
    val checkValidation: () -> Unit = {},
    val navigateToHeader: () -> Unit = {},
    val onLinkClickListener: ((String) -> Unit) = {}
) :
    RecyclerView.Adapter<BaseAddAuthViewHolder<*>>() {


    override fun onBindViewHolder(holder: BaseAddAuthViewHolder<*>, position: Int) {

        holder.checkValidation = checkValidation

        addAuthItems[position].let { addAuthItem ->
            if (addAuthItem.getFieldType() == AddAuthItemType.PLAN_FIELD) {
                when (holder) {
                    is TextFieldViewHolder -> {
                        holder.setFieldRequestValue = ::setFieldRequest
                        holder.isLastEditText = isLastEditText(addAuthItem)
                        holder.bind(addAuthItem)
                    }
                    is SpinnerViewHolder -> {
                        holder.setFieldRequestValue = ::setFieldRequest
                        holder.bind(addAuthItem)
                    }
                    is CheckboxViewHolder -> {
                        holder.setFieldRequestValue = ::setFieldRequest
                        holder.bind(addAuthItem)
                    }
                    is DisplayViewHolder -> {
                        holder.bind(addAuthItem)
                    }
                    else -> {
                    }
                }
            } else if (addAuthItem.getFieldType() == AddAuthItemType.PLAN_DOCUMENT) {
                if (getItemViewType(position) == FieldType.DISPLAY.type) {
                    (holder as DisplayViewHolder).bind(addAuthItem)
                } else {
                    holder.setFieldRequestValue = ::setFieldRequest
                    (holder as CheckboxViewHolder).bind(addAuthItem)
                }
            } else {
                membershipPlan?.let {
                    (holder as HeaderViewHolder).navigateToHeader = navigateToHeader
                    holder.bind(membershipPlan)
                }
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseAddAuthViewHolder<*> {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            FieldType.TEXT.type,
            FieldType.SENSITIVE.type -> {
                TextFieldViewHolder((AddAuthTextItemBinding.inflate(inflater)))
            }
            FieldType.SPINNER.type -> {
                SpinnerViewHolder(
                    AddAuthSpinnerItemBinding.inflate(inflater)
                )
            }
            FieldType.DISPLAY.type -> {
                DisplayViewHolder(
                    AddAuthDisplayItemBinding.inflate(inflater)
                )
            }
            FieldType.HEADER.type -> {
                HeaderViewHolder(
                    AddAuthHeaderItemBinding.inflate(inflater),
                    headerTitle,
                    headerDescription
                ).apply {
                    navigateToHeader = this@AddAuthAdapter.navigateToHeader
                }
            }
            else -> {
                CheckboxViewHolder(AddAuthCheckboxItemBinding.inflate(inflater), onLinkClickListener = onLinkClickListener)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        with(addAuthItems[position]) {
            when {
                getFieldType() == AddAuthItemType.PLAN_FIELD -> (fieldType as PlanField).type?.let {
                    return it
                }
                getFieldType() == AddAuthItemType.PLAN_DOCUMENT -> {
                    val checkBox = (fieldType as PlanDocument).checkbox
                    return if (checkBox == null || checkBox) {
                        FieldType.BOOLEAN_REQUIRED.type
                    } else {
                        FieldType.DISPLAY.type
                    }
                }
                else -> return FieldType.HEADER.type
            }
        }
        return 0
    }

    override fun getItemCount() = addAuthItems.size

    private fun isLastEditText(itemWrapper: AddAuthItemWrapper) =
        itemWrapper == addAuthItems.last { item ->
            item.getFieldType() == AddAuthItemType.PLAN_FIELD &&
                    ((item.fieldType as PlanField).type == FieldType.TEXT.type ||
                            item.fieldType.type == FieldType.SENSITIVE.type)
        }

    private fun setFieldRequest(itemWrapper: AddAuthItemWrapper, value: String) {
        itemWrapper.fieldsRequest?.value = value
    }
}