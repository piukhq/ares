package com.bink.wallet.scenes.add_payment_card

import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bink.android_core.PaymentAccountUtil
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.AddPaymentCardFragmentBinding
import com.bink.wallet.modal.generic.GenericModalParameters
import com.bink.wallet.model.response.payment_card.BankCard
import com.bink.wallet.utils.*
import com.bink.wallet.utils.FirebaseEvents.ADD_PAYMENT_CARD_VIEW
import com.bink.wallet.utils.FirebaseEvents.getFirebaseIdentifier
import com.bink.wallet.utils.UtilFunctions.isNetworkAvailable
import com.bink.wallet.utils.enums.PaymentCardType
import com.bink.wallet.utils.toolbar.FragmentToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel

class AddPaymentCardFragment :
    BaseFragment<AddPaymentCardViewModel, AddPaymentCardFragmentBinding>() {
    companion object {
        const val YEAR_BASE_ADDITION = 2000
    }

    private val addPaymentCardArgs: AddPaymentCardFragmentArgs by navArgs()
    private var cardNumber: String = ""

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .with(binding.toolbar)
            .shouldDisplayBack(requireActivity())
            .build()
    }

    override val viewModel: AddPaymentCardViewModel by viewModel()

    override val layoutRes: Int
        get() = R.layout.add_payment_card_fragment

    override fun onResume() {
        super.onResume()
        logScreenView(ADD_PAYMENT_CARD_VIEW)
    }

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
            if (PaymentAccountUtil.cardValidation(binding.cardNumber.text.toString()).type == PaymentCardType.NONE.type) {
                getString(R.string.incorrect_card_error)
            } else {
                null
            }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.lifecycleOwner = this
        addPaymentCardArgs.cardNumber?.let { safeCardNumber ->
            if (safeCardNumber.isNotEmpty()) {
                cardNumber = safeCardNumber
                viewModel.cardNumber.value = cardNumber
            }
        }
        cardSwitcher(getString(R.string.empty_string))
        cardInfoDisplay()

        binding.addButton.isEnabled = false
        binding.viewModel = viewModel

        viewModel.fetchLocalMembershipPlans()
        viewModel.fetchLocalMembershipCards()

        viewModel.cardNumber.observeNonNull(this) {
            cardSwitcher(it)
            cardInfoDisplay()
        }

        setUpCardInputFormat()

        viewModel.cardHolder.observeNonNull(this) {
            cardInfoDisplay()
        }

        with(binding.cardNumber) {
            filters = arrayOf(
                *this.filters,
                InputFilter.LengthFilter(
                    enumValues<PaymentCardType>().maxByOrNull { it.format.length }?.format?.length
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
                val cardNo = PaymentAccountUtil.numberSanitize(binding.cardNumber.text.toString())
                val cardExp = binding.cardExpiry.text.toString().split("/")

                val bankCard = BankCard(
                    cardNo.substring(0, 6),
                    cardNo.substring(cardNo.length - 4),
                    cardExp[0],
                    (cardExp[1].toInt() + YEAR_BASE_ADDITION).toString(),
                    getString(R.string.country_code_gb),
                    getString(R.string.currency_code_gbp),
                    binding.cardName.text.toString(),
                    PaymentAccountUtil.cardValidation(cardNo).type,
                    PaymentAccountUtil.cardValidation(cardNo).type,
                    PaymentAccountUtil.generateToken(BankCard.TOKEN_LENGTH),
                    PaymentAccountUtil.fingerprintGenerator(cardNo, cardExp[0], cardExp[1])
                )

                findNavController().navigate(
                    AddPaymentCardFragmentDirections.addPaymentToTerms(
                        GenericModalParameters(
                            R.drawable.ic_close,
                            false,
                            getString(R.string.terms_and_conditions_title),
                            getString(R.string.terms_and_conditions_text),
                            getString(R.string.accept_button_text),
                            getString(R.string.decline_button_text)
                        ),
                        bankCard,
                        cardNo
                    )
                )
            }
            logEvent(
                getFirebaseIdentifier(
                    ADD_PAYMENT_CARD_VIEW,
                    binding.addButton.text.toString()
                )
            )
        }

        bindScannedCardNumber()
    }

    override fun onStart() {
        super.onStart()

        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

    override fun onStop() {
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

        super.onStop()
    }

    private fun setUpCardInputFormat() {
        binding.cardNumber.addTextChangedListener(object : TextWatcher {
            private val MAX_SYMBOLS = 19
            private val MAX_DIGITS = 16
            private val MAX_UNSEPARATED_DIGITS = 4
            private val DIVIDER = ' '
            private val DIVIDER_LENGTH = 1

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable) {
                if (s.length <= MAX_SYMBOLS) {
                    if (!isDesiredFormat(
                            s,
                            MAX_SYMBOLS,
                            MAX_UNSEPARATED_DIGITS + DIVIDER_LENGTH,
                            DIVIDER
                        )
                    ) {
                        s.replace(
                            0,
                            s.length,
                            buildCorrectString(
                                getDigitArray(s, MAX_DIGITS),
                                MAX_UNSEPARATED_DIGITS,
                                DIVIDER
                            )
                        )
                    }
                }
            }

            private fun isDesiredFormat(
                s: Editable,
                maxSymbols: Int,
                batchSize: Int,
                divider: Char,
            ): Boolean {
                var isCorrect =
                    s.length <= maxSymbols
                for (i in s.indices) {
                    isCorrect = if (i > 0 && (i + 1) % batchSize == 0) {
                        isCorrect and (divider == s[i])
                    } else {
                        isCorrect and Character.isDigit(s[i])
                    }
                }
                return isCorrect
            }

            private fun buildCorrectString(
                digits: CharArray,
                dividerPosition: Int,
                divider: Char,
            ): String {
                val formatted = StringBuilder()
                for (i in digits.indices) {
                    if (digits[i] != 0.toChar()) {
                        formatted.append(digits[i])
                        if (i > 0 && i < digits.size - 1 && (i + 1) % dividerPosition == 0) {
                            formatted.append(divider)
                        }
                    }
                }
                return formatted.toString()
            }

            private fun getDigitArray(s: Editable, size: Int): CharArray {
                val digits = CharArray(size)
                var index = 0
                var i = 0
                while (i < s.length && index < size) {
                    val current = s[i]
                    if (Character.isDigit(current)) {
                        digits[index] = current
                        index++
                    }
                    i++
                }
                return digits
            }
        })
    }

    private fun cardExpiryErrorCheck(text: String): String? {
        with(text) {
            if (!PaymentAccountUtil.dateValidation(this)) {
                return getString(R.string.incorrect_card_expiry)
            }
            if (!PaymentAccountUtil.formatDate(this).contentEquals(text))
                binding.cardExpiry.setText(PaymentAccountUtil.formatDate(this))
        }
        return null
    }

    private fun cardSwitcher(card: String) {
        PaymentAccountUtil.presentedCardType(card).type.convertToPaymentCardType()?.let { paymentCardType ->
            with(paymentCardType) {
                binding.topLayout.background = ContextCompat.getDrawable(
                    requireContext(),
                    background
                )
                binding.topLayoutBrand.setImageResource(logo)
                binding.bottomLayoutBrand.setImageResource(subLogo)
            }
        }
    }

    private fun cardInfoDisplay() {
        binding.displayCardNumber.text = binding.cardNumber.text.toString().cardStarFormatter()
        binding.displayCardName.text = binding.cardName.text.toString()
    }

    private fun bindScannedCardNumber() {
        if (cardNumber.isNotEmpty()) {
            binding.cardNumber.setText(cardNumber)
        }
    }
}
