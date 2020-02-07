package com.bink.wallet.scenes.add_payment_card

import android.os.Bundle
import android.text.InputFilter
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.AddPaymentCardFragmentBinding
import com.bink.wallet.modal.generic.GenericModalParameters
import com.bink.wallet.model.response.payment_card.BankCard
import com.bink.wallet.utils.*
import com.bink.wallet.utils.UtilFunctions.isNetworkAvailable
import com.bink.wallet.utils.enums.PaymentCardType
import com.bink.wallet.utils.toolbar.FragmentToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.math.min

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

    private val ADD_ANALYTICS_IDENTIFIER = "AddPaymentCardView.Add"

    private fun validateCardName() {
        binding.cardName.error =
            if (binding.cardName.text.isEmpty()) {
                getString(R.string.incorrect_card_name)
            } else {
                null
            }
    }

    private fun validateCardNumber() {
        binding.cardNumber.error =
            if (binding.cardNumber.text.toString().cardValidation() == PaymentCardType.NONE) {
                getString(R.string.incorrect_card_error)
            } else {
                null
            }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        cardSwitcher(getString(R.string.empty_string))
        cardInfoDisplay()

        binding.viewModel = viewModel

        viewModel.cardNumber.observeNonNull(this) {
            cardSwitcher(it)
            cardInfoDisplay()
            updateEnteredCardNumber()
        }

        viewModel.cardHolder.observeNonNull(this) {
            cardInfoDisplay()
        }

        with (binding.cardNumber) {
            filters = arrayOf(
                *this.filters,
                InputFilter.LengthFilter(
                    enumValues<PaymentCardType>().maxBy { it.format.length }?.format?.length
                        ?: 0
                )
            )
            setOnFocusChangeListener { _, focus ->
                if (!focus) {
                    validateCardNumber()
                }
            }
        }

        binding.cardExpiry.setOnFocusChangeListener { _, focus ->
            if (!focus) {
                binding.cardExpiry.error =
                    cardExpiryErrorCheck(viewModel.expiryDate.value ?: EMPTY_STRING)
            }
        }

        binding.cardName.setOnFocusChangeListener { _, focus ->
            if (!focus) {
                validateCardName()
            }
        }

        binding.privacyLink.setOnClickListener {
            findNavController().navigateIfAdded(
                this,
                AddPaymentCardFragmentDirections.actionAddPaymentCardToPrivacyFragment(
                    GenericModalParameters(
                        isCloseModal = true,
                        title = getString(R.string.privacy_and_security),
                        description = getString(R.string.privacy_and_security_description)
                    )
                )
            )
        }

        binding.addButton.setOnClickListener {
            if (isNetworkAvailable(requireActivity(), true)) {
                validateCardName()
                validateCardNumber()
                binding.cardExpiry.error =
                    cardExpiryErrorCheck(viewModel.expiryDate.value ?: EMPTY_STRING)

                if (binding.cardNumber.error.isNullOrEmpty() &&
                    binding.cardExpiry.error.isNullOrBlank() &&
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
                    findNavController().navigateIfAdded(
                        this,
                        AddPaymentCardFragmentDirections.addPaymentToTerms(params, bankCard)
                    )
                }
            }

            logEvent(ADD_ANALYTICS_IDENTIFIER)
        }
    }

    private fun cardExpiryErrorCheck(text: String): String? {
        with(text) {
            if (!dateValidation()) {
                return getString(R.string.incorrect_card_expiry)
            }
            if (!formatDate().contentEquals(text))
                binding.cardExpiry.setText(formatDate())
        }
        return null
    }

    private fun cardSwitcher(card: String) {
        with(card.presentedCardType()) {
            binding.topLayout.background = ContextCompat.getDrawable(
                requireContext(),
                background
            )
            binding.topLayoutBrand.setImageResource(logo)
            binding.bottomLayoutBrand.setImageResource(subLogo)
        }
    }

    private fun cardInfoDisplay() {
        binding.displayCardNumber.text = binding.cardNumber.text.toString().cardStarFormatter()
        binding.displayCardName.text = binding.cardName.text.toString()
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
    private fun updateEnteredCardNumber() {
        with(binding.cardNumber) {
            val origNumber = text.toString()
            val newNumber = origNumber.cardFormatter()
            if (origNumber.isNotEmpty()) {
                if (newNumber.isNotEmpty() &&
                    origNumber != newNumber) {
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
                val sanNumber = origNumber.ccSanitize()
                val type = sanNumber.substring(
                        0,
                        min(4, sanNumber.length)
                    ).presentedCardType()
                val max = type.len
                if (sanNumber.length > max) {
                    val trimmedNumber = if (type == PaymentCardType.NONE) {
                        sanNumber.substring(0, max)
                    } else {
                        sanNumber.substring(0, max).cardFormatter()
                    }
                    setText(trimmedNumber)
                    setSelection(trimmedNumber.length)
                }
            }
        }
    }
}
