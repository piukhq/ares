package com.bink.wallet.scenes.add_auth_enrol.adapter

import android.view.View
import android.widget.AdapterView
import com.bink.wallet.databinding.AddAuthSpinnerItemBinding

import com.bink.wallet.model.response.membership_plan.PlanField
import com.bink.wallet.scenes.add_auth_enrol.AddAuthItemWrapper
import com.bink.wallet.scenes.add_auth_enrol.FormsUtil

class SpinnerViewHolder(
    val binding: AddAuthSpinnerItemBinding
) :
    BaseAddAuthViewHolder<AddAuthItemWrapper>(binding) {

    private var item: AddAuthItemWrapper? = null

    private val itemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {
            position?.let { FormsUtil.updateValidation(it, false) }
        }

        override fun onItemSelected(
            parent: AdapterView<*>?,
            view: View?,
            pos: Int,
            id: Long
        ) {
            item?.let {
                position?.let { position ->
                    FormsUtil.updateField(position, (it.fieldType as PlanField).choice?.get(pos).toString())
                    FormsUtil.updateValidation(position, true)
                }
            }
        }

    }

    override fun bind(item: AddAuthItemWrapper) {
        this.item = item
        val spinner = binding.contentAddAuthSpinner
        val planField = item.fieldType as PlanField
        binding.planField = planField
        position?.let {
            FormsUtil.addFormField(it, planField)
            FormsUtil.updateField(it, planField.choice?.get(0).toString())
            FormsUtil.updateValidation(it, true)
        }

        with(spinner) {
            isFocusable = false
            onItemSelectedListener = itemSelectedListener
        }

        binding.executePendingBindings()
    }
}