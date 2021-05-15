package com.bink.wallet.scenes.add_auth_enrol.adapter

import com.bink.wallet.databinding.AddAuthCheckboxItemBinding
import com.bink.wallet.model.response.membership_plan.PlanDocument
import com.bink.wallet.model.response.membership_plan.PlanField
import com.bink.wallet.scenes.add_auth_enrol.AddAuthItemWrapper
import com.bink.wallet.utils.UtilFunctions
import com.bink.wallet.utils.enums.AddAuthItemType

class CheckboxViewHolder(
    val binding: AddAuthCheckboxItemBinding,
    val onLinkClickListener: ((String) -> Unit)
) :
    BaseAddAuthViewHolder<AddAuthItemWrapper>(binding) {

    override fun bind(item: AddAuthItemWrapper) {
        with(binding) {
            item.fieldsRequest?.let {
                contentAddAuthCheckbox.isChecked = if (it.value == true.toString()) {
                    true
                } else {
                    if (it.value.isNullOrBlank()) {
                        it.value = false.toString()
                    }
                    false
                }
            }
            setDescriptionText(this, item)

            contentAddAuthCheckbox.setOnCheckedChangeListener { _, isChecked ->
                handleCheckBoxChange(item, isChecked)
            }
            contentAddAuthCheckbox.isFocusable = false
        }
        binding.executePendingBindings()
    }

    private fun handleCheckBoxChange(item: AddAuthItemWrapper, isChecked: Boolean) {
        addFormField(item.fieldType as PlanField)
        setFieldRequestValue(item, isChecked.toString())
        checkValidation(null)
    }

    private fun setDescriptionText(
        binding: AddAuthCheckboxItemBinding,
        item: AddAuthItemWrapper
    ) {
        if (item.getFieldType() == AddAuthItemType.PLAN_FIELD) {
            binding.addAuthCheckboxText.text =
                (item.fieldType as PlanField).description
        } else {
            (item.fieldType as PlanDocument).let {
                it.description?.let { description ->
                    binding.addAuthCheckboxText.text = description
                    it.name?.let { name ->
                        it.url?.let { url ->
                            UtilFunctions.buildHyperlinkSpanString(
                                description.plus(
                                    " ${it.name}"
                                ),
                                name,
                                url,
                                binding.addAuthCheckboxText,
                                onLinkClickListener
                            )
                        }
                    }
                }
            }
        }
    }
}