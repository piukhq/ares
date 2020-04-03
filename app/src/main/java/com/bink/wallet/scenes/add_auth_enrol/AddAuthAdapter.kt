package com.bink.wallet.scenes.add_auth_enrol

import android.text.InputFilter
import android.text.Spanned
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
import com.bink.wallet.model.response.membership_plan.PlanDocument
import com.bink.wallet.model.response.membership_plan.PlanField
import com.bink.wallet.utils.SimplifiedTextWatcher
import com.bink.wallet.utils.UtilFunctions
import com.bink.wallet.utils.enums.FieldType
import com.bink.wallet.utils.logError
import com.google.android.material.textfield.TextInputEditText
import java.util.Locale


@Suppress("UNCHECKED_CAST")
class AddAuthAdapter(
    val brands: List<Pair<Any, PlanFieldsRequest>>,
    val buttonRefresh: () -> Unit = {}
) :
    RecyclerView.Adapter<BaseViewHolder<*>>() {

    private var finalTextField: String = ""

    init {
        brands.map { pair ->
            if (pair.first is PlanField &&
                (pair.first as PlanField).type == FieldType.TEXT.type
            ) {
                (pair.first as PlanField).column?.let { column ->
                    finalTextField = column
                }
            }
        }
    }

    private fun checkIfError(position: Int, text: AppCompatEditText) {

        val currentItem = brands[position]

        if (!UtilFunctions.isValidField(
                (currentItem.first as PlanField).validation,
                currentItem.second.value
            )
        ) {
            text.error = text.context.getString(
                R.string.add_auth_error_message,
                (currentItem.first as PlanField).column
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
            if (this is PlanField && type != null) {
                return type
            } else if (this is PlanDocument) {
                return if (this.checkbox == null || this.checkbox) {
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
            if (it.first is PlanField) {
                when (holder) {
                    is TextFieldHolder -> holder.bind(it as Pair<PlanField, PlanFieldsRequest>)
                    is SpinnerViewHolder -> holder.bind(it as Pair<PlanField, PlanFieldsRequest>)
                    is CheckBoxHolder -> holder.bind(it as Pair<PlanField, PlanFieldsRequest>)
                    is DisplayHolder -> holder.bind(it as Pair<PlanField, PlanFieldsRequest>)
                }
            } else {
                when (getItemViewType(position)) {
                    FieldType.DISPLAY.type ->
                        (holder as DisplayHolder).bind(it as Pair<PlanDocument, PlanFieldsRequest>)
                    else ->
                        (holder as CheckBoxHolder).bind(it as Pair<PlanDocument, PlanFieldsRequest>)
                }
            }
        }
    }

    override fun getItemCount() = brands.size

    inner class TextFieldHolder(val binding: AddAuthTextItemBinding) :
        BaseViewHolder<Pair<PlanField, PlanFieldsRequest>>(binding) {

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

        private val emailTextWatcher = object : SimplifiedTextWatcher {
            override fun onTextChanged(
                currentText: CharSequence?,
                p1: Int,
                p2: Int,
                p3: Int
            ) {
                brands[adapterPosition].second.value =
                    currentText.toString().toLowerCase(Locale.ROOT)
                buttonRefresh()
            }
        }

        override fun bind(item: Pair<PlanField, PlanFieldsRequest>) {
            binding.planField = item.first
            with(binding.contentAddAuthText) {
                hint = item.first.description
                setText(item.second.value)
                item.second.disabled?.let {
                    if (it) {
                        isEnabled = false
                    }
                }
                if (item.first.common_name == COMMON_NAME_EMAIL) {
                    binding.contentAddAuthText.filters = arrayOf(object : InputFilter.AllCaps() {
                        override fun filter(
                            source: CharSequence?,
                            start: Int,
                            end: Int,
                            dest: Spanned?,
                            dstart: Int,
                            dend: Int
                        ): CharSequence {
                            return source.toString().toLowerCase(Locale.ROOT)
                        }
                    })
                    addTextChangedListener(emailTextWatcher)
                } else {
                    addTextChangedListener(textWatcher)
                }
                if (brands[adapterPosition].second.value.isNullOrBlank()) {
                    error = null
                } else {
                    checkIfError(adapterPosition, this)
                }

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
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        checkIfFieldIsValid()
                    }
                    false
                }
            }

            binding.executePendingBindings()
        }

        private fun TextInputEditText.checkIfFieldIsValid() {
            try {
                checkIfError(adapterPosition, this)
                buttonRefresh()
            } catch (ex: Exception) {
                logError(AddAuthAdapter::class.simpleName, "Invalid regex : $ex")
            }
        }
    }

    inner class SpinnerViewHolder(val binding: AddAuthSpinnerItemBinding) :
        BaseViewHolder<Pair<PlanField, PlanFieldsRequest>>(binding) {

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


    inner class CheckBoxHolder(val binding: AddAuthSwitchItemBinding) :
        BaseViewHolder<Pair<Any, PlanFieldsRequest>>(binding) {

        override fun bind(item: Pair<Any, PlanFieldsRequest>) {

            with(binding.contentAddAuthSwitch) {
                brands[adapterPosition].second.apply {
                    isChecked = if (this.value == true.toString()) {
                        true
                    } else {
                        if (this.value.isNullOrBlank()) {
                            this.value = false.toString()
                        }
                        false
                    }
                }

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

    companion object {
        private const val COMMON_NAME_EMAIL = "email"
    }
}