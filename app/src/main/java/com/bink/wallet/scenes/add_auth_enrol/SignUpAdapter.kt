package com.bink.wallet.scenes.add_auth_enrol

import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.CheckBox
import android.widget.Spinner
import androidx.appcompat.widget.AppCompatEditText
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.R
import com.bink.wallet.databinding.AddAuthSpinnerItemBinding
import com.bink.wallet.databinding.AddAuthSwitchItemBinding
import com.bink.wallet.databinding.AddAuthSwitchItemBindingImpl
import com.bink.wallet.databinding.AddAuthTextItemBinding
import com.bink.wallet.model.response.membership_plan.PlanFields
import com.bink.wallet.utils.UtilFunctions
import com.bink.wallet.utils.enums.FieldType
import com.bink.wallet.utils.enums.TypeOfField


class SignUpAdapter(
    private val brands: List<Pair<PlanFields, com.bink.wallet.model.request.membership_card.PlanFields>>
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

    override fun onBindViewHolder(holder: CardFieldHolder, position: Int) {
        brands[position].let { holder.bind(it) }
    }

    override fun getItemCount(): Int {
        return brands.size
    }

    class CardFieldHolder(val binding: ViewDataBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var text: AppCompatEditText? = null
        private var switch: CheckBox? = null
        private var spinner: Spinner? = null

        init {
            text = itemView.findViewById(R.id.content_add_auth_text)
            switch = itemView.findViewById(R.id.content_add_auth_switch)
            spinner = itemView.findViewById(R.id.content_add_auth_spinner)
        }

        fun bind(item: Pair<PlanFields, com.bink.wallet.model.request.membership_card.PlanFields>) {
            val currentAuthoriseField = item.second

            when (binding) {
                is AddAuthTextItemBinding -> {
                    binding.planField = item.first
                    text?.hint = item.first.description

                    text?.addTextChangedListener(object : TextWatcher {
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
                            currentAuthoriseField.value = currentText.toString()
                        }
                    })

                    text?.setOnFocusChangeListener { _, isFocus ->
                        if (!isFocus)
                            try {
                                if (!UtilFunctions.isValidField(
                                        item.first.validation, currentAuthoriseField.value
                                    )
                                )
                                    text?.error = text?.resources?.getString(
                                        R.string.add_auth_error_message,
                                        item.first.column
                                    )
                            } catch (ex: Exception) {
                                Log.e(SignUpAdapter::class.simpleName, "Invalid regex : $ex")
                            }
                    }
                }


                is AddAuthSpinnerItemBinding -> {
                    binding.planField = item.first
                    currentAuthoriseField.value = item.first.choice?.get(0)
                    spinner?.isFocusable = false
                    spinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(parent: AdapterView<*>?) {

                        }

                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            currentAuthoriseField.value = item.first.choice?.get(position)
                        }

                    }
                }
                is AddAuthSwitchItemBinding -> {
                    binding.planField = item.first

                    currentAuthoriseField.value = FALSE_TEXT

                    switch?.text = item.first.description

                    switch?.setOnCheckedChangeListener { _, isChecked ->
                        currentAuthoriseField.value = isChecked.toString()
                    }

                    switch?.isFocusable = false
                }
            }

//            when (item.first.typeOfField) {
//                TypeOfField.ADD -> account.add_fields?.add(currentAuthoriseField)
//                TypeOfField.AUTH -> account.authorise_fields?.add(currentAuthoriseField)
//                TypeOfField.ENROL -> account.enrol_fields?.add(currentAuthoriseField)
//                TypeOfField.REGISTRATION -> account.registration_fields?.add(currentAuthoriseField)
//            }

            binding.executePendingBindings()
        }
    }
}