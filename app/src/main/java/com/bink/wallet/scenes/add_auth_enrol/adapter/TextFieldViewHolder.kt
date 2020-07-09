import android.app.DatePickerDialog
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.text.InputType
import android.view.View
import android.view.inputmethod.EditorInfo
import com.bink.wallet.R
import com.bink.wallet.databinding.AddAuthTextItemBinding
import com.bink.wallet.model.response.membership_plan.PlanField
import com.bink.wallet.scenes.add_auth_enrol.AddAuthItemWrapper
import com.bink.wallet.scenes.add_auth_enrol.adapter.AddAuthAdapter
import com.bink.wallet.scenes.add_auth_enrol.adapter.BaseAddAuthViewHolder
import com.bink.wallet.utils.DATE_FORMAT
import com.bink.wallet.utils.SimplifiedTextWatcher
import com.bink.wallet.utils.UtilFunctions
import com.bink.wallet.utils.enums.AddAuthItemType
import com.bink.wallet.utils.enums.SignUpFieldTypes
import com.bink.wallet.utils.logError
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import android.text.method.PasswordTransformationMethod
import android.view.MotionEvent
import android.widget.Toast

class TextFieldViewHolder(
    val binding: AddAuthTextItemBinding
) :
    BaseAddAuthViewHolder<AddAuthItemWrapper>(binding) {

    var isLastEditText: Boolean = false
    var item: AddAuthItemWrapper? = null

    private val textWatcher = object : SimplifiedTextWatcher {
        override fun onTextChanged(
            currentText: CharSequence?,
            p1: Int,
            p2: Int,
            p3: Int
        ) {
            item?.let {
                setFieldRequestValue(it, currentText.toString())
            }
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
            item?.let {
                setFieldRequestValue(
                    it,
                    currentText.toString().toLowerCase(Locale.ROOT)
                )
            }
            checkValidation()
        }
    }

    override fun bind(item: AddAuthItemWrapper) {
        this.item = item

        val planField = item.fieldType as PlanField
        val planRequest = item.fieldsRequest

        binding.planField = planField

        with(binding.contentAddAuthText) {
            planField.description?.length?.let {
                hint = planField.description
            }
            item.fieldType.common_name?.let {
                displayCustomKeyboard(it)
                createDateAndShowPicker(it)
            }

            setText(planRequest?.value)

            planRequest?.disabled?.let {
                if (it) {
                    isEnabled = false
                }
            }

            if (planField.common_name == SignUpFieldTypes.EMAIL.common_name) {
                addTextChangedListener(emailTextWatcher)
            } else {
                addTextChangedListener(textWatcher)
            }
            if (planRequest?.value.isNullOrBlank()) {
                error = null
            } else {
                checkIfFieldIsValid(item)
            }

            imeOptions =
                if (isLastEditText) {
                    EditorInfo.IME_ACTION_DONE
                } else {
                    EditorInfo.IME_ACTION_NEXT
                }

            setOnFocusChangeListener { _, isFocus ->
                if (!isFocus) {
                    checkIfFieldIsValid(item)
                }
            }
            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    checkIfFieldIsValid(item)
                }
                false
            }

            if (planField.common_name.equals(CARD_NUMBER) && text.toString().trim()
                    .isEmpty() && hasBarcodeCommonName()
            ) {
                setEndDrawable(context.getDrawable(R.drawable.ic_camera))

                onTouchListener(false)

                addTextChangedListener(object : SimplifiedTextWatcher {
                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                        if (s.toString().trim().isNotEmpty()) {
                            setEndDrawable(context.getDrawable(R.drawable.ic_clear_search))
                            onTouchListener(true)
                        } else {
                            setEndDrawable(context.getDrawable(R.drawable.ic_camera))
                            onTouchListener(false)
                        }
                    }

                })
            } else {
                setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
            }


        }

        planRequest?.let { safePlan ->
            if (safePlan.shouldIgnore) {
                binding.titleAddAuthText.visibility = View.GONE
                binding.separator.visibility = View.GONE
                binding.contentAddAuthText.visibility = View.GONE
            }
        }

        binding.executePendingBindings()
    }

    private fun TextInputEditText.setEndDrawable(drawable: Drawable?) {
        setCompoundDrawablesRelativeWithIntrinsicBounds(
            null,
            null,
            drawable,
            null
        )
    }

    private fun TextInputEditText.clearField() {
        this.text?.clear()
    }

    private fun TextInputEditText.onTouchListener(shouldClearText: Boolean) {
        setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                if (event?.action == MotionEvent.ACTION_UP) {
                    if (event.rawX >= (binding.contentAddAuthText.right - binding.contentAddAuthText.compoundDrawables[DRAWABLE_END].bounds.width())) {
                        if (shouldClearText) {
                            Toast.makeText(context, "Clear", Toast.LENGTH_SHORT).show()
                            clearField()

                        } else {
                            Toast.makeText(context, "Barcode scanner", Toast.LENGTH_SHORT).show()
                            binding.titleAddAuthText.setTextColor(Color.GRAY)

                        }
                        return true
                    }
                }
                return false
            }

        })
    }

    private fun hasBarcodeCommonName(): Boolean {
        //Loop through plan fields alternatives
        val alternativeValues = mutableListOf<String>()

        //Get all the alternatives
        addFields?.forEach { planField ->
            (planField.alternatives?.forEach { alternative ->
                alternativeValues.add(alternative)
            })
        }

        //Check if any of the alternatives have "barcode" as column name
        alternativeValues.forEach { alternative ->

            addFields?.let { addFields ->
                addFields.forEach { planField ->
                    if (planField.column.equals(alternative) && planField.common_name.equals(BARCODE)) {
                        return true
                    }
                }
            }
        }

        return false
    }


    private fun TextInputEditText.checkIfFieldIsValid(currentItem: AddAuthItemWrapper) {
        try {
            checkIfError(this, currentItem)
            checkValidation()
        } catch (ex: Exception) {
            logError(AddAuthAdapter::class.simpleName, "Invalid regex : $ex")
            error = context?.getString(
                R.string.add_auth_error_message,
                ex.message
            )
        }
    }

    private fun checkIfError(text: TextInputEditText, currentItem: AddAuthItemWrapper) {
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

    private fun TextInputEditText.displayCustomKeyboard(commonName: String) {
        when (commonName) {
            SignUpFieldTypes.PASSWORD.common_name -> {
                transformationMethod = PasswordTransformationMethod()
            }
            SignUpFieldTypes.PHONE.common_name,
            SignUpFieldTypes.PHONE_NUMBER_1.common_name,
            SignUpFieldTypes.PHONE_NUMBER_2.common_name -> {
                inputType = InputType.TYPE_CLASS_NUMBER
            }
            SignUpFieldTypes.EMAIL.common_name -> {
                inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            }
            else -> {
                inputType = InputType.TYPE_CLASS_TEXT
            }
        }

    }

    private fun createDateAndShowPicker(commonName: String) {
        if (commonName == SignUpFieldTypes.DATE_OF_BIRTH.common_name || commonName == SignUpFieldTypes.MEMORABLE_DATE.common_name) {
            val datePickerDialog = DatePickerDialog(
                binding.root.context, R.style.BinkDatePicker,
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
                    binding.tvDatePicker.text = strDate.toString()
                    item?.let {
                        setFieldRequestValue(it, strDate.toString())
                    }
                    checkValidation()
                },
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
            )
            binding.contentAddAuthText.visibility = View.INVISIBLE
            binding.tvDatePicker.visibility = View.VISIBLE

            binding.tvDatePicker.setOnClickListener {
                datePickerDialog.show()
                datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE)
                    .setTextColor(Color.BLACK)
                datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE)
                    .setTextColor(Color.BLACK)
            }
        } else {
            binding.contentAddAuthText.visibility = View.VISIBLE
            binding.tvDatePicker.visibility = View.GONE
        }
    }

    companion object {
        private const val BARCODE = "barcode"
        private const val CARD_NUMBER = "card_number"
        private const val DRAWABLE_END = 2
    }
}