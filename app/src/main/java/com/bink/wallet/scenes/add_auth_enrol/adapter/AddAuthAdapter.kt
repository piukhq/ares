package com.bink.wallet.scenes.add_auth_enrol.adapter

import CheckboxViewHolder
import DisplayViewHolder
import SpinnerViewHolder
import TextFieldViewHolder
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.databinding.AddAuthDisplayItemBinding
import com.bink.wallet.databinding.AddAuthSpinnerItemBinding
import com.bink.wallet.databinding.AddAuthSwitchItemBinding
import com.bink.wallet.databinding.AddAuthTextItemBinding
import com.bink.wallet.model.request.membership_card.PlanFieldsRequest
import com.bink.wallet.model.response.membership_plan.PlanDocument
import com.bink.wallet.model.response.membership_plan.PlanField
import com.bink.wallet.utils.enums.FieldType


@Suppress("UNCHECKED_CAST")
class AddAuthAdapter(
    val brands: List<Pair<Any, PlanFieldsRequest>>,
    val buttonRefresh: () -> Unit = {}
) :
    RecyclerView.Adapter<BaseAddAuthViewHolder<*>>() {
    override fun onBindViewHolder(holder: BaseAddAuthViewHolder<*>, position: Int) {
        holder.brands = brands.toMutableList()
        holder.buttonRefresh = buttonRefresh

        brands[position].let {
            if (it.first is PlanField) {
                when (holder) {
                    is TextFieldViewHolder -> holder.bind(it as Pair<PlanField, PlanFieldsRequest>)
                    is SpinnerViewHolder -> holder.bind(it as Pair<PlanField, PlanFieldsRequest>)
                    is CheckboxViewHolder -> holder.bind(it as Pair<PlanField, PlanFieldsRequest>)
                    is DisplayViewHolder -> holder.bind(it as Pair<PlanField, PlanFieldsRequest>)
                }
            } else {
                when (getItemViewType(position)) {
                    FieldType.DISPLAY.type ->
                        (holder as DisplayViewHolder).bind(it as Pair<PlanDocument, PlanFieldsRequest>)
                    else ->
                        (holder as CheckboxViewHolder).bind(it as Pair<PlanDocument, PlanFieldsRequest>)
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
            else -> CheckboxViewHolder(AddAuthSwitchItemBinding.inflate(inflater))
        }
    }

    override fun getItemViewType(position: Int): Int {
        brands[position].first.apply {
            if (this is PlanField && type != null) {
                return type
            } else if (this is PlanDocument) {
                return if (checkbox == null || checkbox) {
                    FieldType.BOOLEAN_REQUIRED.type
                } else {
                    FieldType.DISPLAY.type
                }
            }
        }
        return 0
    }

    override fun getItemCount() = brands.size

    companion object {
        const val COMMON_NAME_EMAIL = "email"
    }
}