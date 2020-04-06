import com.bink.wallet.databinding.AddAuthDisplayItemBinding
import com.bink.wallet.model.request.membership_card.PlanFieldsRequest
import com.bink.wallet.model.response.membership_plan.PlanDocument
import com.bink.wallet.model.response.membership_plan.PlanField
import com.bink.wallet.scenes.BaseViewHolder
import com.bink.wallet.scenes.add_auth_enrol.adapter.BaseAddAuthViewHolder
import com.bink.wallet.utils.UtilFunctions

class DisplayViewHolder(val binding: AddAuthDisplayItemBinding) :
    BaseAddAuthViewHolder<Pair<Any, PlanFieldsRequest>>(binding) {

    override fun bind(item: Pair<Any, PlanFieldsRequest>) {
        with(binding.contentAddAuthDisplay) {
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
        }
        binding.executePendingBindings()
    }
}