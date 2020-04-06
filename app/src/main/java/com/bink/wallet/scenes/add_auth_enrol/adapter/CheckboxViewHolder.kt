import com.bink.wallet.databinding.AddAuthSwitchItemBinding
import com.bink.wallet.model.request.membership_card.PlanFieldsRequest
import com.bink.wallet.model.response.membership_plan.PlanDocument
import com.bink.wallet.model.response.membership_plan.PlanField
import com.bink.wallet.scenes.add_auth_enrol.adapter.BaseAddAuthViewHolder
import com.bink.wallet.utils.UtilFunctions

class CheckboxViewHolder(
    val binding: AddAuthSwitchItemBinding
) :
    BaseAddAuthViewHolder<Pair<Any, PlanFieldsRequest>>(binding) {

    override fun bind(item: Pair<Any, PlanFieldsRequest>) {

        with(binding.contentAddAuthSwitch) {
            brands[adapterPosition].second.apply {
                isChecked = if (value == true.toString()) {
                    true
                } else {
                    if (value.isNullOrBlank()) {
                        value = false.toString()
                    }
                    false
                }
            }

            when (item.first) {
                is PlanField ->
                    text =
                        (item.first as PlanField).description
                else -> {
                    (item.first as PlanDocument).let {
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
            }
            setOnCheckedChangeListener { _, isChecked ->
                brands[adapterPosition].second.value = isChecked.toString()
                buttonRefresh()
            }
            isFocusable = false
        }
        binding.executePendingBindings()
    }
}