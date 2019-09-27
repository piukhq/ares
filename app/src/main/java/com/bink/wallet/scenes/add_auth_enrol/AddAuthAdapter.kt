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
import com.bink.wallet.model.request.membership_card.Account
import com.bink.wallet.model.response.membership_plan.AddFields
import com.bink.wallet.model.response.membership_plan.AuthoriseFields
import com.bink.wallet.model.response.membership_plan.EnrolFields
import com.bink.wallet.utils.UtilFunctions
import com.bink.wallet.utils.enums.FieldType
import com.bink.wallet.utils.enums.FieldViewType


class AddAuthAdapter(
    private val brands: List<Any>,
    private var account: Account
) :
    RecyclerView.Adapter<BaseViewHolder<*>>() {

    companion object {
        const val FALSE_TEXT = "false"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        val inflater = LayoutInflater.from(parent.context)

        val binding: ViewDataBinding

        binding = when (viewType % 10) {
            FieldType.TEXT.type,
            FieldType.PASSWORD.type -> AddAuthTextItemBinding.inflate(inflater)
            FieldType.SPINNER.type -> AddAuthSpinnerItemBinding.inflate(inflater)
            else -> AddAuthSwitchItemBindingImpl.inflate(inflater)
        }

        return when (viewType / 10) {
            FieldViewType.ADD.type -> AddFieldHolder(binding, account)
            FieldViewType.AUTH.type -> AuthFieldHolder(binding, account)
            else -> EnrolFieldHolder(binding, account)
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        when (holder) {
            is AddFieldHolder -> brands[position].let { holder.bind(it as AddFields) }
            is AuthFieldHolder -> brands[position].let { holder.bind(it as AuthoriseFields) }
            is EnrolFieldHolder -> brands[position].let { holder.bind(it as EnrolFields) }
        }
    }

    override fun getItemCount(): Int {
        return brands.size
    }

    override fun getItemViewType(position: Int): Int {
        return when (brands[position]) {
            is AddFields -> FieldViewType.ADD.type * 10 + (brands[position] as AddFields).type!!
            is AuthoriseFields -> FieldViewType.AUTH.type * 10 + (brands[position] as AuthoriseFields).type!!
            else -> FieldViewType.ENROL.type * 10 + (brands[position] as EnrolFields).type!!
        }
    }

    class AddFieldHolder(val binding: ViewDataBinding, val account: Account) :
        BaseViewHolder<AddFields>(binding) {

        private var text: AppCompatEditText? = null
        private var switch: CheckBox? = null
        private var spinner: Spinner? = null

        init {
            text = itemView.findViewById(R.id.content_add_auth_text)
            switch = itemView.findViewById(R.id.content_add_auth_switch)
            spinner = itemView.findViewById(R.id.content_add_auth_spinner)
        }

        override fun bind(item: AddFields) {
            val currentAddField =
                com.bink.wallet.model.request.membership_card.AddFields(item.column, "")
            when (binding) {
                is AddAuthTextItemBinding -> {
                    binding.addFields = item

                    text?.hint = item.description
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
                            currentAddField.value = currentText.toString()
                        }
                    })

                    text?.setOnFocusChangeListener { _, isFocus ->
                        if (!isFocus)
                            try {
                                if (!UtilFunctions.isValidField(
                                        item.validation, currentAddField.value
                                    )
                                )
                                    text?.error = text?.resources?.getString(
                                        R.string.add_auth_error_message,
                                        item.column
                                    )
                            } catch (ex: Exception) {
                                Log.e(AddAuthAdapter::class.simpleName, "Invalid regex : $ex")
                            }
                    }
                }
                is AddAuthSpinnerItemBinding -> {
                    binding.addFields = item

                    currentAddField.value = item.choice?.get(0)

                    spinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(parent: AdapterView<*>?) {

                        }

                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            currentAddField.value = item.choice?.get(position)
                        }

                    }
                    spinner?.isFocusable = false
                }
                is AddAuthSwitchItemBinding -> {
                    binding.addFields = item

                    switch?.text = item.description

                    currentAddField.value = FALSE_TEXT

                    switch?.setOnCheckedChangeListener { _, isChecked ->
                        currentAddField.value = isChecked.toString()
                    }
                    switch?.isFocusable = false

                }
            }
            account.add_fields?.add(currentAddField)

            binding.executePendingBindings()
        }
    }

    class EnrolFieldHolder(val binding: ViewDataBinding, val account: Account) :
        BaseViewHolder<EnrolFields>(binding) {

        private var text: AppCompatEditText? = null
        private var switch: CheckBox? = null
        private var spinner: Spinner? = null

        init {
            text = itemView.findViewById(R.id.content_add_auth_text)
            switch = itemView.findViewById(R.id.content_add_auth_switch)
            spinner = itemView.findViewById(R.id.content_add_auth_spinner)
        }

        override fun bind(item: EnrolFields) {
            val currentEnrolField =
                com.bink.wallet.model.request.membership_card.EnrolFields(item.column, "")
            when (binding) {
                is AddAuthTextItemBinding -> {
                    binding.enrolFields = item

                    text?.hint = item.description
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
                            currentEnrolField.value = currentText.toString()
                        }
                    })

                    text?.setOnFocusChangeListener { _, isFocus ->
                        if (!isFocus)
                            try {
                                if (!UtilFunctions.isValidField(
                                        item.validation, currentEnrolField.value
                                    )
                                )
                                    text?.error = text?.resources?.getString(
                                        R.string.add_auth_error_message,
                                        item.column
                                    )
                            } catch (ex: Exception) {
                                Log.e(AddAuthAdapter::class.simpleName, "Invalid regex : $ex")
                            }
                    }
                }
                is AddAuthSpinnerItemBinding -> {
                    binding.enrolFields = item

                    currentEnrolField.value = item.choice?.get(0)

                    spinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(parent: AdapterView<*>?) {

                        }

                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            currentEnrolField.value = item.choice?.get(position)
                        }

                    }
                    spinner?.isFocusable = false
                }
                is AddAuthSwitchItemBinding -> {
                    binding.enrolFields = item

                    switch?.text = item.description

                    currentEnrolField.value = FALSE_TEXT

                    switch?.setOnCheckedChangeListener { _, isChecked ->
                        currentEnrolField.value = isChecked.toString()
                    }
                    switch?.isFocusable = false

                }
            }
            account.enrol_fields?.add(currentEnrolField)

            binding.executePendingBindings()
        }
    }


    class AuthFieldHolder(val binding: ViewDataBinding, val account: Account) :
        BaseViewHolder<AuthoriseFields>(binding) {

        private var text: AppCompatEditText? = null
        private var switch: CheckBox? = null
        private var spinner: Spinner? = null

        init {
            text = itemView.findViewById(R.id.content_add_auth_text)
            switch = itemView.findViewById(R.id.content_add_auth_switch)
            spinner = itemView.findViewById(R.id.content_add_auth_spinner)
        }

        override fun bind(item: AuthoriseFields) {
            val currentAuthoriseField =
                com.bink.wallet.model.request.membership_card.AuthoriseFields(
                    item.column, ""
                )
            when (binding) {
                is AddAuthTextItemBinding -> {

                    binding.authFields = item
                    text?.hint = item.description
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
                                        item.validation, currentAuthoriseField.value
                                    )
                                )
                                    text?.error = text?.resources?.getString(
                                        R.string.add_auth_error_message,
                                        item.column
                                    )
                            } catch (ex: Exception) {
                                Log.e(AddAuthAdapter::class.simpleName, "Invalid regex : $ex")
                            }
                    }
                }


                is AddAuthSpinnerItemBinding -> {
                    binding.authFields = item
                    currentAuthoriseField.value = item.choice?.get(0)
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
                            currentAuthoriseField.value = item.choice?.get(position)
                        }

                    }
                }
                is AddAuthSwitchItemBinding -> {
                    binding.authFields = item

                    currentAuthoriseField.value = FALSE_TEXT

                    switch?.text = item.description

                    switch?.setOnCheckedChangeListener { _, isChecked ->
                        currentAuthoriseField.value = isChecked.toString()
                    }

                    switch?.isFocusable = false
                }
            }
            account.authorise_fields?.add(currentAuthoriseField)
            binding.executePendingBindings()
        }
    }
}