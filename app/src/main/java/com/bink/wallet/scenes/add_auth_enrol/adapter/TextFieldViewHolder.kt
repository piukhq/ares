import android.text.InputFilter
import android.text.Spanned
import android.view.inputmethod.EditorInfo
import com.bink.wallet.databinding.AddAuthTextItemBinding
import com.bink.wallet.model.request.membership_card.PlanFieldsRequest
import com.bink.wallet.model.response.membership_plan.PlanField
import com.bink.wallet.scenes.add_auth_enrol.adapter.AddAuthAdapter
import com.bink.wallet.scenes.add_auth_enrol.adapter.BaseAddAuthViewHolder
import com.bink.wallet.utils.SimplifiedTextWatcher
import com.bink.wallet.utils.logError
import com.google.android.material.textfield.TextInputEditText
import java.util.*

class TextFieldViewHolder(
    val binding: AddAuthTextItemBinding) :
    BaseAddAuthViewHolder<Pair<PlanField, PlanFieldsRequest>>(binding) {

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
            if (item.first.common_name == AddAuthAdapter.COMMON_NAME_EMAIL) {
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