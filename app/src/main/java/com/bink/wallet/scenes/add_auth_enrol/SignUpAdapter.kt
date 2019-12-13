package com.bink.wallet.scenes.add_auth_enrol

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.appcompat.widget.AppCompatEditText
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.R
import com.bink.wallet.databinding.AddAuthSpinnerItemBinding
import com.bink.wallet.databinding.AddAuthSwitchItemBinding
import com.bink.wallet.databinding.AddAuthTextItemBinding
import com.bink.wallet.model.request.membership_card.PlanFieldsRequest
import com.bink.wallet.model.response.membership_plan.PlanFields
import com.bink.wallet.utils.SimplifiedTextWatcher
import com.bink.wallet.utils.UtilFunctions
import com.bink.wallet.utils.enums.FieldType


class SignUpAdapter(
    val brands: List<Pair<PlanFields, PlanFieldsRequest>>
) :
    RecyclerView.Adapter<BaseViewHolder<*>>() {

    companion object {
        const val FALSE_TEXT = "false"
    }

    private fun checkIfError(item: PlanFields, position: Int, text: AppCompatEditText) {

        val currentItem = brands[position]

        if (!UtilFunctions.isValidField(
                currentItem.first.validation,
                currentItem.second.value
            )
        ) {
            text.error = text.resources?.getString(
                R.string.add_auth_error_message,
                item.column
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            FieldType.TEXT.type,
            FieldType.PASSWORD.type -> TextFieldHolder(AddAuthTextItemBinding.inflate(inflater))
            FieldType.SPINNER.type -> SpinnerViewHolder(AddAuthSpinnerItemBinding.inflate(inflater))
            else -> CheckBoxHolder(AddAuthSwitchItemBinding.inflate(inflater))
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (brands[position].first.type != null)
            return brands[position].first.type!!
        return 0
    }


    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        brands[position].let {
            when (holder) {
                is TextFieldHolder -> holder.bind(it)
                is SpinnerViewHolder -> holder.bind(it)
                is CheckBoxHolder -> holder.bind(it)
            }
        }
    }

    override fun getItemCount(): Int {
        return brands.size
    }

    inner class TextFieldHolder(val binding: AddAuthTextItemBinding) :
        BaseViewHolder<Pair<PlanFields, PlanFieldsRequest>>(binding) {

        private val textWatcher = object : SimplifiedTextWatcher {
            override fun onTextChanged(
                currentText: CharSequence?,
                p1: Int,
                p2: Int,
                p3: Int
            ) {
                brands[adapterPosition].second.value = currentText.toString()
            }
        }

        override fun bind(item: Pair<PlanFields, PlanFieldsRequest>) {
            binding.planField = item.first
            val text = binding.contentAddAuthText
            with(text) {
                hint = item.first.description
                setText(item.second.value)
                item.second.disabled?.let {
                    if (it) {
                        isEnabled = false
                    }
                }
                addTextChangedListener(textWatcher)
                if (brands[adapterPosition].second.value.isNullOrBlank())
                    error = null
                else
                    checkIfError(brands[adapterPosition].first, adapterPosition, this)

                setOnFocusChangeListener { _, isFocus ->
                    if (!isFocus) {
                        setText(getText().toString().trim())
                        try {
                            checkIfError(brands[adapterPosition].first, adapterPosition, this)
                        } catch (ex: Exception) {
                            Log.e(SignUpAdapter::class.simpleName, "Invalid regex : $ex")
                        }
                    }
                }
            }

            binding.executePendingBindings()
        }
    }

    inner class SpinnerViewHolder(val binding: AddAuthSpinnerItemBinding) :
        BaseViewHolder<Pair<PlanFields, PlanFieldsRequest>>(binding) {

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

        override fun bind(item: Pair<PlanFields, PlanFieldsRequest>) {
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


    inner class CheckBoxHolder(val binding: AddAuthSwitchItemBinding) :
        BaseViewHolder<Pair<PlanFields, PlanFieldsRequest>>(binding) {

        override fun bind(item: Pair<PlanFields, PlanFieldsRequest>) {
            val switch = binding.contentAddAuthSwitch
            binding.planField = item.first
            brands[adapterPosition].second.value = FALSE_TEXT
            with(switch) {
                text = item.first.description
                setOnCheckedChangeListener { _, isChecked ->
                    brands[adapterPosition].second.value = isChecked.toString()
                }
                isFocusable = false
            }
            binding.executePendingBindings()
        }
    }
}