package com.bink.wallet.scenes.add_payment_card

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.core.content.ContextCompat
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.AddPaymentCardFragmentBinding
import com.bink.wallet.model.payment_card.PaymentCardType
import com.bink.wallet.utils.presentedCardType
import com.bink.wallet.utils.toolbar.FragmentToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel

class AddPaymentCardFragment :
    BaseFragment<AddPaymentCardViewModel, AddPaymentCardFragmentBinding>() {

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .withId(FragmentToolbar.NO_TOOLBAR)
            .build()
    }

    override val viewModel: AddPaymentCardViewModel by viewModel()

    override val layoutRes: Int
        get() = R.layout.add_payment_card_fragment

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
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
            }
        }
        binding.cardNumber.addTextChangedListener(textWatcher)
    }

    fun cardSwitcher(card: String) {
        when (card.presentedCardType()) {
            PaymentCardType.NONE -> {}
            PaymentCardType.VISA -> {
                binding.topLayout.background = ContextCompat.getDrawable(requireContext(), R.drawable.ic_card_payment_bkgr_visa)
                binding.topLayoutBrand.background = ContextCompat.getDrawable(requireContext(), R.drawable.ic_card_payment_logo_visa)
                binding.bottomLayoutBrand.background = ContextCompat.getDrawable(requireContext(), R.drawable.ic_card_payment_sublogo_visa)
            }
            PaymentCardType.MASTERCARD -> {
                binding.topLayout.background = ContextCompat.getDrawable(requireContext(), R.drawable.ic_card_payment_bkgr_master_card)
                binding.topLayoutBrand.background = ContextCompat.getDrawable(requireContext(), R.drawable.ic_card_payment_logo_master_card)
                binding.bottomLayoutBrand.background = ContextCompat.getDrawable(requireContext(), R.drawable.ic_card_payment_sublogo_master_card)
            }
            PaymentCardType.AMEX -> {
                binding.topLayout.background = ContextCompat.getDrawable(requireContext(), R.drawable.ic_card_payment_bkgr_am_ex)
                binding.topLayoutBrand.background = ContextCompat.getDrawable(requireContext(), R.drawable.ic_card_payment_logo_am_ex)
                binding.bottomLayoutBrand.background = ContextCompat.getDrawable(requireContext(), R.drawable.ic_card_payment_sublogo_am_ex)
            }
        }
    }
}
