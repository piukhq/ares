import android.app.DatePickerDialog
import android.text.InputFilter
import android.text.InputType
import android.text.Spanned
import android.view.inputmethod.EditorInfo
import androidx.appcompat.widget.AppCompatEditText
import com.bink.wallet.R
import com.bink.wallet.databinding.AddAuthTextItemBinding
import com.bink.wallet.model.response.membership_plan.PlanField
import com.bink.wallet.scenes.add_auth_enrol.AddAuthItemWrapper
import com.bink.wallet.scenes.add_auth_enrol.adapter.AddAuthAdapter
import com.bink.wallet.scenes.add_auth_enrol.adapter.BaseAddAuthViewHolder
import com.bink.wallet.utils.DATE_FORMAT
import com.bink.wallet.utils.EMPTY_STRING
import com.bink.wallet.utils.SimplifiedTextWatcher
import com.bink.wallet.utils.UtilFunctions
import com.bink.wallet.utils.enums.AddAuthItemType
import com.bink.wallet.utils.enums.FieldType
import com.bink.wallet.utils.enums.SignUpFieldTypes
import com.bink.wallet.utils.logError
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.*
import java.util.Locale



class TextFieldViewHolder(
    val binding: AddAuthTextItemBinding
) :
    BaseAddAuthViewHolder<AddAuthItemWrapper>(binding) {

    private var finalTextField: String = EMPTY_STRING

    init {
        addAuthItems.map { item ->
            if (item.getFieldType() == AddAuthItemType.PLAN_FIELD &&
                (item.fieldType as PlanField).type == FieldType.TEXT.type
            ) {
                item.fieldType.column?.let { column ->
                    finalTextField = column
                }
            }
        }
    }

    private val textWatcher = object : SimplifiedTextWatcher {
        override fun onTextChanged(
            currentText: CharSequence?,
            p1: Int,
            p2: Int,
            p3: Int
        ) {
            addAuthItems[adapterPosition].fieldsRequest?.value = currentText.toString()
            checkValidation()
        }
    }

    private val emailTextWatcher = object : SimplifiedTextWatcher {
        override fun onTextChanged(
            currentText: CharSequence?,
            p1: Int,
            p2: Int,
            p3: Int
        ) {
            addAuthItems[adapterPosition].fieldsRequest?.value =
                currentText.toString().toLowerCase(Locale.ROOT)
            checkValidation()
        }
    }

    override fun bind(item: AddAuthItemWrapper) {
        val planField = item.fieldType as PlanField
        val planRequest = item.fieldsRequest

        binding.planField = planField

        with(binding.contentAddAuthText) {
            planField.description?.length?.let {
                hint = planField.description
            }
            item.fieldType.common_name?.let {
                displayCustomKeyboard(it)
            }

            setText(planRequest?.value)

            planRequest?.disabled?.let {
                if (it) {
                    isEnabled = false
                }
            }

            if (planField.common_name == SignUpFieldTypes.EMAIL.common_name) {
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
            if (planRequest?.value.isNullOrBlank()) {
                error = null
            } else {
                checkIfFieldIsValid()
            }

            imeOptions =
                if (planRequest?.column == finalTextField) {
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

    private fun TextInputEditText.displayCustomKeyboard(commonName: String) {
        inputType = when (commonName) {
            SignUpFieldTypes.PHONE.common_name,
            SignUpFieldTypes.PHONE_NUMBER_1.common_name,
            SignUpFieldTypes.PHONE_NUMBER_2.common_name -> {
                InputType.TYPE_CLASS_NUMBER
            }
            SignUpFieldTypes.EMAIL.common_name -> {
                InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            }
            SignUpFieldTypes.DATE_OF_BIRTH.common_name,
            SignUpFieldTypes.MEMORABLE_DATE.common_name -> {
                val datePickerDialog = DatePickerDialog(
                    binding.root.context,
                    DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                        val calendar = Calendar.getInstance()
                        calendar.set(Calendar.MONTH, month)
                        calendar.set(Calendar.YEAR, year)
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                        val date = Date(calendar.timeInMillis)

                        val dateFormatter = SimpleDateFormat(
                            DATE_FORMAT, Locale.ENGLISH
                        )
                        val strDate = dateFormatter.format(date)
                        setText(
                            strDate.toString()
                        )
                        checkValidation()
                    },
                    Calendar.getInstance().get(Calendar.YEAR),
                    Calendar.getInstance().get(Calendar.MONTH),
                    Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
                )
                setOnClickListener {
                    datePickerDialog.show()
                }
                InputType.TYPE_NULL
            }
            else -> {
                InputType.TYPE_CLASS_TEXT
            }
        }
    }

    private fun TextInputEditText.checkIfFieldIsValid() {
        try {
            checkIfError(adapterPosition, this)
            checkValidation()
        } catch (ex: Exception) {
            logError(AddAuthAdapter::class.simpleName, "Invalid regex : $ex")
            error = context?.getString(
                R.string.add_auth_error_message,
                ex.message
            )
        }
    }

    private fun checkIfError(position: Int, text: AppCompatEditText) {
        val currentItem = addAuthItems[position]
        if (currentItem.getFieldType() == AddAuthItemType.PLAN_FIELD) {
            val currentPlanField = currentItem.fieldType as PlanField
            val requestValue = currentItem.fieldsRequest?.value
            if (!UtilFunctions.isValidField(
                    currentPlanField.validation,
                    requestValue
                )
            ) {
                text.error = text.context.getString(
                    R.string.add_auth_error_message,
                    currentPlanField.column
                )
            }
        }
    }
}