package com.bink.wallet.scenes.add_auth_enrol

import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.appcompat.widget.AppCompatEditText
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.R
import com.bink.wallet.databinding.AddAuthSpinnerItemBinding
import com.bink.wallet.databinding.AddAuthSwitchItemBinding
import com.bink.wallet.databinding.AddAuthSwitchItemBindingImpl
import com.bink.wallet.databinding.AddAuthTextItemBinding
import com.bink.wallet.model.request.membership_card.PlanFieldsRequest
import com.bink.wallet.model.response.membership_plan.PlanFields
import com.bink.wallet.utils.UtilFunctions
import com.bink.wallet.utils.enums.FieldType


class SignUpAdapter(
    val brands: List<Pair<PlanFields, PlanFieldsRequest>>
) :
    RecyclerView.Adapter<SignUpAdapter.CardFieldHolder>() {

    companion object {
        const val FALSE_TEXT = "false"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardFieldHolder {
        val inflater = LayoutInflater.from(parent.context)

        val binding: ViewDataBinding

        binding = when (viewType) {
            FieldType.TEXT.type,
            FieldType.PASSWORD.type -> AddAuthTextItemBinding.inflate(inflater)
            FieldType.SPINNER.type -> AddAuthSpinnerItemBinding.inflate(inflater)
            else -> AddAuthSwitchItemBindingImpl.inflate(inflater)
        }

        return CardFieldHolder(binding)
    }

    override fun getItemViewType(position: Int): Int {
        if (brands[position].first.type != null)
            return brands[position].first.type!!
        return 0
    }


    override fun onBindViewHolder(holder: CardFieldHolder, position: Int) {
        brands[position].let { holder.bind(it) }
    }

    override fun getItemCount(): Int {
        return brands.size
    }

    inner class CardFieldHolder(val binding: ViewDataBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val textWatcher = object : TextWatcher {

            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(
                p0: CharSequence?,
                p1: Int,
                p2: Int,
                p3: Int
            ) {
            }

            override fun onTextChanged(
                currentText: CharSequence?,
                p1: Int,
                p2: Int,
                p3: Int
            ) {
                brands[adapterPosition].second.value = currentText.toString()
            }
        }

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
                    brands[adapterPosition].first.choice?.get(position)
            }

        }

        private fun checkIfError(item: PlanFields, text: AppCompatEditText) {

            if (!UtilFunctions.isValidField(
                    brands[adapterPosition].first.validation, brands[adapterPosition].second.value
                )
            )
                text.error = text.resources?.getString(
                    R.string.add_auth_error_message,
                    item.column
                )
        }

        fun bind(item: Pair<PlanFields, PlanFieldsRequest>) {

            when (binding) {
                is AddAuthTextItemBinding -> {
                    binding.planField = item.first
                    val text = binding.contentAddAuthText
                    text?.hint = item.first.description
                    text?.setText(item.second.value)
                    text?.addTextChangedListener(textWatcher)
                    if (brands[adapterPosition].second.value.isNullOrBlank())
                        text?.error = null
                    else
                        checkIfError(brands[adapterPosition].first, text)

                    text?.setOnFocusChangeListener { _, isFocus ->
                        if (!isFocus)
                            try {
                                checkIfError(brands[adapterPosition].first, text)
                            } catch (ex: Exception) {
                                Log.e(SignUpAdapter::class.simpleName, "Invalid regex : $ex")
                            }
                    }
                }

                is AddAuthSpinnerItemBinding -> {
                    val spinner = binding.contentAddAuthSpinner
                    binding.planField = item.first
                    brands[adapterPosition].second.value = item.first.choice?.get(0)
                    spinner?.isFocusable = false
                    spinner?.onItemSelectedListener = itemSelectedListener
                }
                is AddAuthSwitchItemBinding -> {
                    val switch = binding.contentAddAuthSwitch
                    binding.planField = item.first
                    brands[adapterPosition].second.value = FALSE_TEXT
                    switch?.text = item.first.description

                    switch?.setOnCheckedChangeListener { _, isChecked ->
                        brands[adapterPosition].second.value = isChecked.toString()
                    }

                    switch?.isFocusable = false
                }
            }

            binding.executePendingBindings()
        }
    }
}