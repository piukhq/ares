import android.view.View
import android.widget.AdapterView
import com.bink.wallet.databinding.AddAuthSpinnerItemBinding

import com.bink.wallet.model.response.membership_plan.PlanField
import com.bink.wallet.scenes.add_auth_enrol.AddAuthItemWrapper
import com.bink.wallet.scenes.add_auth_enrol.adapter.BaseAddAuthViewHolder

class SpinnerViewHolder(
    val binding: AddAuthSpinnerItemBinding
) :
    BaseAddAuthViewHolder<AddAuthItemWrapper>(binding) {

    private val itemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {

        }

        override fun onItemSelected(
            parent: AdapterView<*>?,
            view: View?,
            position: Int,
            id: Long
        ) {
            addAuthItems[adapterPosition].fieldsRequest?.value =
                (addAuthItems[adapterPosition].fieldType as PlanField).choice?.get(position)
        }

    }

    override fun bind(item: AddAuthItemWrapper) {
        val spinner = binding.contentAddAuthSpinner
        val planField = item.fieldType as PlanField
        binding.planField = planField
        addAuthItems[adapterPosition].fieldsRequest?.value = planField.choice?.get(0)
        with(spinner) {
            isFocusable = false
            onItemSelectedListener = itemSelectedListener
        }
        binding.executePendingBindings()
    }
}