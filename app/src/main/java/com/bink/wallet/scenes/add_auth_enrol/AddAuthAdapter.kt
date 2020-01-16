package com.bink.wallet.scenes.add_auth_enrol

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import androidx.appcompat.widget.AppCompatEditText
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.R
import com.bink.wallet.databinding.AddAuthDisplayItemBinding
import com.bink.wallet.databinding.AddAuthSpinnerItemBinding
import com.bink.wallet.databinding.AddAuthSwitchItemBinding
import com.bink.wallet.databinding.AddAuthTextItemBinding
import com.bink.wallet.model.request.membership_card.PlanFieldsRequest
import com.bink.wallet.model.response.membership_plan.PlanDocuments
import com.bink.wallet.model.response.membership_plan.PlanFields
import com.bink.wallet.utils.SimplifiedTextWatcher
import com.bink.wallet.utils.UtilFunctions
import com.bink.wallet.utils.enums.FieldType


class AddAuthAdapter(
    val brands: List<Pair<Any, PlanFieldsRequest>>,
    val buttonRefresh: () -> Unit = {}
) :
    RecyclerView.Adapter<BaseViewHolder<*>>() {

    private var finalTextField: String = ""
    init {
        brands.map {
            if (it.first is PlanFields) {
                finalTextField = (it.first as PlanFields).column!!
            }
        }
    }

    private fun checkIfError(position: Int, text: AppCompatEditText) {

        val currentItem = brands[position]

        if (!UtilFunctions.isValidField(
                (currentItem.first as PlanFields).validation,
                currentItem.second.value
            )
        ) {
            text.error = text.resources?.getString(
                R.string.add_auth_error_message,
                (currentItem.first as PlanFields).column
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            FieldType.TEXT.type,
            FieldType.PASSWORD.type -> TextFieldHolder(AddAuthTextItemBinding.inflate(inflater))
            FieldType.SPINNER.type -> SpinnerViewHolder(AddAuthSpinnerItemBinding.inflate(inflater))
            FieldType.DISPLAY.type -> DisplayHolder(AddAuthDisplayItemBinding.inflate(inflater))
            else -> CheckBoxHolder(AddAuthSwitchItemBinding.inflate(inflater))
        }
    }

    override fun getItemViewType(position: Int): Int {
        brands[position].first.apply {
            if (this is PlanFields && type != null) {
                return type
            } else if (this is PlanDocuments) {
                return if (this.checkbox == null ||
                           this.checkbox) {
                    FieldType.BOOLEAN_REQUIRED.type
                } else {
                    FieldType.DISPLAY.type
                }
            }
        }
        return 0
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        brands[position].let {
            if (it.first is PlanFields) {
                when (holder) {
                    is TextFieldHolder -> holder.bind(it as Pair<PlanFields, PlanFieldsRequest>)
                    is SpinnerViewHolder -> holder.bind(it as Pair<PlanFields, PlanFieldsRequest>)
                    is CheckBoxHolder -> holder.bind(it as Pair<PlanFields, PlanFieldsRequest>)
                    is DisplayHolder -> holder.bind(it as Pair<PlanFields, PlanFieldsRequest>)
                }
            } else {
                when (getItemViewType(position)) {
                    FieldType.DISPLAY.type ->
                        (holder as DisplayHolder).bind(it as Pair<PlanDocuments, PlanFieldsRequest>)
                    else ->
                        (holder as CheckBoxHolder).bind(it as Pair<PlanDocuments, PlanFieldsRequest>)
                }
            }
        }
    }

    override fun getItemCount() = brands.size

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
                buttonRefresh()
            }
        }

        override fun bind(item: Pair<PlanFields, PlanFieldsRequest>) {
            binding.planField = item.first
            with(binding.contentAddAuthText) {
                hint = item.first.description
                setText(item.second.value)
                addTextChangedListener(textWatcher)
                if (brands[adapterPosition].second.value.isNullOrBlank())
                    error = null
                else
                    checkIfError(adapterPosition, this)

                imeOptions =
                    if (item.second.column == finalTextField) {
                        EditorInfo.IME_ACTION_DONE
                    } else {
                        EditorInfo.IME_ACTION_NEXT
                    }
                setOnFocusChangeListener { _, isFocus ->
                    if (!isFocus) {
                        checkIfFieldIsValid()
                    }
                }
                setOnEditorActionListener { _, actionId, _ ->
                    if(actionId == EditorInfo.IME_ACTION_DONE){
                        checkIfFieldIsValid()
                        false
                    } else {
                        true
                    }
                }
            }

            binding.executePendingBindings()
        }

        private fun AppCompatEditText.checkIfFieldIsValid() {
            try {
                checkIfError(adapterPosition, this)
                buttonRefresh()
            } catch (ex: Exception) {
                Log.e(AddAuthAdapter::class.simpleName, "Invalid regex : $ex")
            }
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
                    (brands[adapterPosition].first as PlanFields).choice?.get(position)
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
        BaseViewHolder<Pair<Any, PlanFieldsRequest>>(binding) {

        override fun bind(item: Pair<Any, PlanFieldsRequest>) {

            with(binding.contentAddAuthSwitch) {
                isChecked = when (brands[adapterPosition].second.value) {
                    true.toString() -> true
                    else -> false
                }

                when (item.first) {
                    is PlanFields ->
                        text =
                            (item.first as PlanFields).description
                    else -> {
                        (item.first as PlanDocuments).let {
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

    inner class DisplayHolder(val binding: AddAuthDisplayItemBinding) :
        BaseViewHolder<Pair<Any, PlanFieldsRequest>>(binding) {

        override fun bind(item: Pair<Any, PlanFieldsRequest>) {

            with(binding.contentAddAuthDisplay) {
                when (item.first) {
                    is PlanFields ->
                        text =
                            (item.first as PlanFields).description
                    else -> {
                        (item.first as PlanDocuments).let {
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
}