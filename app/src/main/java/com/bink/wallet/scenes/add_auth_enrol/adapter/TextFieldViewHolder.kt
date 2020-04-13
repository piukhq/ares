import android.text.InputFilter
import android.text.Spanned
import android.view.inputmethod.EditorInfo
import com.bink.wallet.databinding.AddAuthTextItemBinding
import com.bink.wallet.model.response.membership_plan.PlanField
import com.bink.wallet.scenes.add_auth_enrol.AddAuthItemWrapper
import com.bink.wallet.scenes.add_auth_enrol.adapter.AddAuthAdapter
import com.bink.wallet.scenes.add_auth_enrol.adapter.BaseAddAuthViewHolder
import com.bink.wallet.utils.EMPTY_STRING
import com.bink.wallet.utils.SimplifiedTextWatcher
import com.bink.wallet.utils.enums.AddAuthItemType
import com.bink.wallet.utils.enums.FieldType
import com.bink.wallet.utils.logError
import com.google.android.material.textfield.TextInputEditText
import java.util.*

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
            hint = planField.description

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

    private fun TextInputEditText.checkIfFieldIsValid() {
        try {
            checkIfError(adapterPosition, this)
            checkValidation()
        } catch (ex: Exception) {
            logError(AddAuthAdapter::class.simpleName, "Invalid regex : $ex")
        }
    }
}