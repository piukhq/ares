package com.bink.wallet.scenes.add_payment_card

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.AddPaymentCardFragmentBinding
import com.bink.wallet.utils.enums.PaymentCardType
import com.bink.wallet.model.response.payment_card.Account
import com.bink.wallet.model.response.payment_card.BankCard
import com.bink.wallet.model.response.payment_card.Consent
import com.bink.wallet.model.response.payment_card.PaymentCardAdd
import com.bink.wallet.scenes.wallets.WalletsFragmentDirections
import com.bink.wallet.utils.*
import com.bink.wallet.utils.toolbar.FragmentToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel

class AddPaymentCardFragment :
    BaseFragment<AddPaymentCardViewModel, AddPaymentCardFragmentBinding>() {
    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .with(binding.toolbar)
            .shouldDisplayBack(activity!!)
            .build()
    }

    override val viewModel: AddPaymentCardViewModel by viewModel()

    override val layoutRes: Int
        get() = R.layout.add_payment_card_fragment

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        cardSwitcher("")
        cardInfoDisplay()

        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(
                p0: CharSequence?,
                p1: Int,
                p2: Int,
                p3: Int
            ) {
            }

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
        binding.cardNumber.addTextChangedListener(textWatcher)
        binding.cardNumber.setOnFocusChangeListener { _, focus ->
            if (!focus) {
                binding.cardNumberInputLayout.error =
                    if (binding.cardNumber.text.toString().cardValidation() == PaymentCardType.NONE) {
                        getString(R.string.incorrect_card_error)
                    } else {
                        ""
                    }
            }
        }

        binding.cardExpiry.setOnFocusChangeListener { view, focus ->
            if (!focus) {
                binding.cardExpiryInputLayout.error = cardExpiryErrorCheck(view)
            }
        }
        binding.cardName.setOnFocusChangeListener { view, focus ->
            if (!focus) {
                binding.cardNameInputLayout.error =
                    if (binding.cardName.text.toString().isEmpty()) {
                        getString(R.string.incorrect_card_name)
                    } else {
                        ""
                    }
            }
        }

        binding.addButton.setOnClickListener {
            if (binding.cardNumberInputLayout.error.isNullOrEmpty() &&
                binding.cardExpiryInputLayout.error.isNullOrBlank() &&
                !binding.cardName.text.isNullOrEmpty()) {

                val cardNo = binding.cardNumber.text.toString().numberSanitize()
                val cardExp = binding.cardExpiry.text.toString().split("/")

                // TODO add in location request and get lat & long from the SDK
                viewModel.sendAddCard(
                    PaymentCardAdd(
                        BankCard(
                            cardNo.substring(0, 6),
                            cardNo.substring(cardNo.length - 4),
                            cardExp[0].toInt(),
                            cardExp[1].toInt(),
                            "GB",
                            "GBP",
                            binding.cardName.text.toString(),
                            cardNo.cardValidation().type,
                            cardNo.cardValidation().type,
                            BankCard.tokenGenerator(),
                            BankCard.fingerprintGenerator(cardNo, cardExp[0], cardExp[1])
                        ),
                        Account(
                            false,
                            0,
                            listOf(
                                Consent(
                                    0,
                                    0.0f,
                                    0.0f,
                                    System.currentTimeMillis()
                                )
                            )
                        )
                    )
                )
            }
        }

        viewModel.paymentCard.observeNonNull(this) {
            val action =
                AddPaymentCardFragmentDirections.addPaymentToDetails(
                    it,
                    arrayOf(),
                    arrayOf()
                )
            findNavController().navigateIfAdded(
                this@AddPaymentCardFragment,
                action
            )
        }
        viewModel.error.observeNonNull(this) {
            if (viewModel.error.value != null) {
                requireContext().displayModalPopup(
                    getString(R.string.add_card_error_title),
                    getString(R.string.add_card_error_message) + "\n" + viewModel.error.value!!.message
                )
            }
        }
    }

    fun cardExpiryErrorCheck(view: View): String {
        with ((view as EditText).text.toString()) {
            if (!dateValidation()) {
                return getString(R.string.incorrect_card_expiry)
            } else {
                binding.cardExpiry.setText(formatDate())
            }
        }
        return ""
    }

    fun cardSwitcher(card: String) {
        with (card.presentedCardType()) {
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

    fun updateEnteredCardNumber() {
        with (binding.cardNumber) {
            val origNumber = text.toString()
            val newNumber = origNumber.cardFormatter()
            if (origNumber.isNotEmpty() && newNumber.isNotEmpty()) {
                if (origNumber != newNumber) {
                    val pos = selectionStart
                    setText(newNumber)
                    if (newNumber.length > origNumber.length) {
                        if (pos == origNumber.length) {
                            setSelection(newNumber.length)
                        }
                    } else if (newNumber.length < origNumber.length) {
                        if (pos > newNumber.length) {
                            setSelection(newNumber.length)
                        }
                    }
                }
            }
        }
    }
}
