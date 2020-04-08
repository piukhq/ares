import com.bink.wallet.databinding.AddAuthCheckboxItemBinding

import com.bink.wallet.model.response.membership_plan.PlanDocument
import com.bink.wallet.model.response.membership_plan.PlanField
import com.bink.wallet.scenes.add_auth_enrol.AddAuthItemWrapper
import com.bink.wallet.scenes.add_auth_enrol.adapter.BaseAddAuthViewHolder
import com.bink.wallet.utils.UtilFunctions
import com.bink.wallet.utils.enums.AddAuthItemType

class CheckboxViewHolder(
    val binding: AddAuthCheckboxItemBinding
) :
    BaseAddAuthViewHolder<AddAuthItemWrapper>(binding) {

    override fun bind(item: AddAuthItemWrapper) {
        with(binding) {
            item.fieldsRequest.apply {
                contentAddAuthCheckbox.isChecked = if (value == true.toString()) {
                    true
                } else {
                    if (value.isNullOrBlank()) {
                        value = false.toString()
                    }
                    false
                }
            }

            setDescriptionText(this, item)

            contentAddAuthCheckbox.setOnCheckedChangeListener { _, isChecked ->
                handleCheckBoxChange(isChecked)
            }
            addAuthCheckboxText.setOnClickListener {
                val isChecked = contentAddAuthCheckbox.isChecked
                contentAddAuthCheckbox.isChecked = !isChecked
                handleCheckBoxChange(contentAddAuthCheckbox.isChecked)
            }
            contentAddAuthCheckbox.isFocusable = false
        }
        binding.executePendingBindings()
    }

    private fun handleCheckBoxChange(isChecked: Boolean) {
        addAuthItems[adapterPosition].fieldsRequest.value = isChecked.toString()
        checkValidation()
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
                                binding.addAuthCheckboxText
                            )
                        }
                    }
                }
            }
        }
    }
}