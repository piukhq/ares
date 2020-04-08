import com.bink.wallet.databinding.AddAuthSwitchItemBinding
import com.bink.wallet.model.response.membership_plan.PlanDocument
import com.bink.wallet.model.response.membership_plan.PlanField
import com.bink.wallet.scenes.add_auth_enrol.AddAuthItemWrapper
import com.bink.wallet.scenes.add_auth_enrol.adapter.BaseAddAuthViewHolder
import com.bink.wallet.utils.UtilFunctions
import com.bink.wallet.utils.enums.AddAuthItemType

class CheckboxViewHolder(
    val binding: AddAuthSwitchItemBinding
) :
    BaseAddAuthViewHolder<AddAuthItemWrapper>(binding) {


    override fun bind(item: AddAuthItemWrapper) {

        with(binding.contentAddAuthSwitch) {
            item.fieldsRequest.apply {
                isChecked = if (value == true.toString()) {
                    true
                } else {
                    if (value.isNullOrBlank()) {
                        value = false.toString()
                    }
                    false
                }
            }

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
                                    this
                                )
                            }
                        }
                    }
                }
            }

            setOnCheckedChangeListener { _, isChecked ->
                addAuthItems[adapterPosition].fieldsRequest.value = isChecked.toString()
                checkValidation()
            }
            isFocusable = false
        }
        binding.executePendingBindings()
    }
}