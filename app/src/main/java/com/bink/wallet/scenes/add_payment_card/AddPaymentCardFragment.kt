package com.bink.wallet.scenes.add_payment_card

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.core.content.ContextCompat
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.AddPaymentCardFragmentBinding
import com.bink.wallet.model.payment_card.PaymentCardType
import com.bink.wallet.utils.cardFormatter
import com.bink.wallet.utils.cardStarFormatter
import com.bink.wallet.utils.presentedCardType
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
