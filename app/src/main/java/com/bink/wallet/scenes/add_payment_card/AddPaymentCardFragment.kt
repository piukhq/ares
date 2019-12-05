package com.bink.wallet.scenes.add_payment_card

import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.AddPaymentCardFragmentBinding
import com.bink.wallet.modal.generic.GenericModalParameters
import com.bink.wallet.model.response.payment_card.BankCard
import com.bink.wallet.utils.*
import com.bink.wallet.utils.enums.PaymentCardType
import com.bink.wallet.utils.toolbar.FragmentToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel

class AddPaymentCardFragment :
    BaseFragment<AddPaymentCardViewModel, AddPaymentCardFragmentBinding>() {
    companion object {
        const val YEAR_BASE_ADDITION = 2000
    }

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .with(binding.toolbar)
            .shouldDisplayBack(requireActivity())
            .build()
    }

    override val viewModel: AddPaymentCardViewModel by viewModel()

    override val layoutRes: Int
        get() = R.layout.add_payment_card_fragment

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        cardSwitcher(getString(R.string.empty_string))
        cardInfoDisplay()

        val cardNumberTextWatcher = object : SimplifiedTextWatcher {
            override fun onTextChanged(
                currentText: CharSequence?,
                p1: Int,
                p2: Int,
                p3: Int
            ) {
                cardSwitcher(currentText.toString())
                cardInfoDisplay()
                updateEnteredCardNumber()
            }
        }
        with(binding.cardNumber) {
            addTextChangedListener(cardNumberTextWatcher)
            setOnFocusChangeListener { _, focus ->
                if (!focus) {
                    binding.cardNumberInputLayout.error =
                        if (text.toString().cardValidation() == PaymentCardType.NONE) {
                            getString(R.string.incorrect_card_error)
                        } else {
                            getString(R.string.empty_string)
                        }
                }
            }
        }

        binding.cardExpiry.setOnFocusChangeListener { view, focus ->
            if (!focus) {
                binding.cardExpiryInputLayout.error = cardExpiryErrorCheck(view)
            }
        }

        val nameTextWatcher = object : SimplifiedTextWatcher {
            override fun onTextChanged(
                currentText: CharSequence?,
                p1: Int,
                p2: Int,
                p3: Int
            ) {
                binding.displayCardName.text = currentText
            }
        }
        with(binding.cardName) {
            addTextChangedListener(nameTextWatcher)
            setOnFocusChangeListener { _, focus ->
                if (!focus) {
                    binding.cardNameInputLayout.error =
                        if (binding.cardName.text.toString().isEmpty()) {
                            getString(R.string.incorrect_card_name)
                        } else {
                            getString(R.string.empty_string)
                        }
                }
            }
        }

        binding.privacyLink.setOnClickListener {
            val securityDialog = SecurityDialogs(requireContext())
            securityDialog.openDialog(layoutInflater)
        }

        binding.addButton.setOnClickListener {
            if (binding.cardNumberInputLayout.error.isNullOrEmpty() &&
                binding.cardExpiryInputLayout.error.isNullOrBlank() &&
                !binding.cardName.text.isNullOrEmpty()
            ) {

                val cardNo = binding.cardNumber.text.toString().numberSanitize()
                val cardExp = binding.cardExpiry.text.toString().split("/")

                val bankCard = BankCard(
                    cardNo.substring(0, 6),
                    cardNo.substring(cardNo.length - 4),
                    cardExp[0].toInt(),
                    cardExp[1].toInt() + YEAR_BASE_ADDITION,
                    getString(R.string.country_code_gb),
                    getString(R.string.currency_code_gbp),
                    binding.cardName.text.toString(),
                    cardNo.cardValidation().type,
                    cardNo.cardValidation().type,
                    BankCard.tokenGenerator(),
                    BankCard.fingerprintGenerator(cardNo, cardExp[0], cardExp[1])
                )
                val params = GenericModalParameters(
                    R.drawable.ic_close,
                    true,
                    getString(R.string.terms_and_conditions_title),
                    getString(R.string.terms_and_conditions_text),
                    getString(R.string.accept_button_text),
                    getString(R.string.decline_button_text)
                )
                val action = AddPaymentCardFragmentDirections.addPaymentToTerms(params, bankCard)
                findNavController().navigateIfAdded(this, action)
            }
        }
    }

    private fun cardExpiryErrorCheck(view: View): String {
        with((view as EditText).text.toString()) {
            if (!dateValidation()) {
                return getString(R.string.incorrect_card_expiry)
            }
            binding.cardExpiry.setText(formatDate())
        }
        return getString(R.string.empty_string)
    }

    fun cardSwitcher(card: String) {
        with(card.presentedCardType()) {
            binding.topLayout.background = ContextCompat.getDrawable(
                requireContext(),
                background
            )
            binding.topLayoutBrand.setImageResource(logo)
            binding.bottomLayoutBrand.setImageResource(subLogo)
        }
    }

    fun cardInfoDisplay() {
        binding.displayCardNumber.text = binding.cardNumber.text.toString().cardStarFormatter()
        binding.displayCardName.text = binding.cardName.text
    }

    /***
     * This function is frustrating as hell for the logic... if the user is entering and it adds
     * a space every few, and the cursor has to be moved to the end, but if the user is editing
     * and it has to re-format the number, then the cursor stays in the same place it was!
     * Remember that the spacing can move between a Visa/MC & AmEx, especially if the user
     * decides to add a "3" before the start of a previously Visa number... so the number could
     * go from "4242 4242 4242" to "3424 242424 242", and that causes a bit of insanity on
     * cursor locations!
     */
    fun updateEnteredCardNumber() {
        with(binding.cardNumber) {
            val origNumber = text.toString()
            val newNumber = origNumber.cardFormatter()
            if (origNumber.isNotEmpty() &&
                newNumber.isNotEmpty() &&
                origNumber != newNumber
            ) {
                val pos = selectionStart
                setText(newNumber)
                if (newNumber.length > origNumber.length &&
                    pos == origNumber.length
                ) {
                    setSelection(newNumber.length)
                } else if (newNumber.length < origNumber.length &&
                    pos > newNumber.length
                ) {
                    setSelection(newNumber.length)
                }
            }
        }
    }
}
