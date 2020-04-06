import android.view.View
import android.widget.AdapterView
import com.bink.wallet.databinding.AddAuthSpinnerItemBinding
import com.bink.wallet.model.request.membership_card.PlanFieldsRequest
import com.bink.wallet.model.response.membership_plan.PlanField
import com.bink.wallet.scenes.add_auth_enrol.adapter.BaseAddAuthViewHolder

class SpinnerViewHolder(
    val binding: AddAuthSpinnerItemBinding
) :
    BaseAddAuthViewHolder<Pair<PlanField, PlanFieldsRequest>>(binding) {

    private val itemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {

        }

        override fun onItemSelected(
            parent: AdapterView<*>?,
            view: View?,
            position: Int,
            id: Long
        ) {
            brands[adapterPosition].second.value =
                (brands[adapterPosition].first as PlanField).choice?.get(position)
        }

    }

    override fun bind(item: Pair<PlanField, PlanFieldsRequest>) {
        val spinner = binding.contentAddAuthSpinner
        binding.planField = item.first
        brands[adapterPosition].second.value = item.first.choice?.get(0)
        with(spinner) {
            isFocusable = false
            onItemSelectedListener = itemSelectedListener
        }
        binding.executePendingBindings()
    }
}