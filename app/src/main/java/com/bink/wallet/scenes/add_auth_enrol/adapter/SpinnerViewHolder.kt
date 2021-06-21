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

    private var item: AddAuthItemWrapper? = null

    private val itemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {

        }

        override fun onItemSelected(
            parent: AdapterView<*>?,
            view: View?,
            position: Int,
            id: Long
        ) {
            item?.let {
                setFieldRequestValue(
                    it,
                    (it.fieldType as PlanField).choice?.get(position).toString()
                )
            }
        }

    }

    override fun bind(item: AddAuthItemWrapper) {
        this.item = item
        val spinner = binding.contentAddAuthSpinner
        val planField = item.fieldType as PlanField
        binding.planField = planField
        setFieldRequestValue(item, planField.choice?.get(0).toString())
        with(spinner) {
            isFocusable = false
            onItemSelectedListener = itemSelectedListener
        }
        binding.executePendingBindings()
    }
}