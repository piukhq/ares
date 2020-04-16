import android.text.InputFilter
import android.text.Spanned
import android.view.inputmethod.EditorInfo
import com.bink.wallet.R
import com.bink.wallet.databinding.AddAuthTextItemBinding
import com.bink.wallet.model.response.membership_plan.PlanField
import com.bink.wallet.scenes.add_auth_enrol.AddAuthItemWrapper
import com.bink.wallet.scenes.add_auth_enrol.adapter.AddAuthAdapter
import com.bink.wallet.scenes.add_auth_enrol.adapter.BaseAddAuthViewHolder
import com.bink.wallet.utils.SimplifiedTextWatcher
import com.bink.wallet.utils.UtilFunctions
import com.bink.wallet.utils.enums.AddAuthItemType
import com.bink.wallet.utils.logError
import com.google.android.material.textfield.TextInputEditText
import java.util.*


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

            setText(planRequest?.value)

            planRequest?.disabled?.let {
                if (it) {
                    isEnabled = false
                }
            }

            if (planField.common_name == AddAuthAdapter.COMMON_NAME_EMAIL) {
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
        }

        binding.executePendingBindings()
    }

    private fun TextInputEditText.checkIfFieldIsValid(currentItem: AddAuthItemWrapper) {
        try {
            checkIfError( this, currentItem)
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
}