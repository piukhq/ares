package com.bink.wallet.scenes.add_auth_enrol.adapter

import com.bink.wallet.databinding.AddAuthDisplayItemBinding
import com.bink.wallet.model.response.membership_plan.PlanDocument
import com.bink.wallet.model.response.membership_plan.PlanField
import com.bink.wallet.scenes.add_auth_enrol.AddAuthItemWrapper
import com.bink.wallet.utils.UtilFunctions
import com.bink.wallet.utils.enums.AddAuthItemType

class DisplayViewHolder(
    val binding: AddAuthDisplayItemBinding, val onLinkClickListener: ((String) -> Unit)
) :
    BaseAddAuthViewHolder<AddAuthItemWrapper>(binding) {

    override fun bind(item: AddAuthItemWrapper) {
        with(binding.contentAddAuthDisplay) {
            if (item.getFieldType() == AddAuthItemType.PLAN_FIELD) {
                text =
                    (item.fieldType as PlanField).description
            } else {
                (item.fieldType as PlanDocument).let {
                    it.description?.let { description ->
                        text = description
                        it.name?.let { name ->
                            it.url?.let { url ->
                                UtilFunctions.buildHyperlinkSpanString(
                                    description.plus(
                                        " ${it.name}"
                                    ),
                                    name,
                                    url,
                                    this,
                                    onLinkClickListener
                                )
                            }
                        }
                    }
                }
            }
        }
        binding.executePendingBindings()
    }
}