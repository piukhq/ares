package com.bink.wallet.scenes.loyalty_wallet.barcode

import android.os.Bundle
import android.view.View
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.BarcodeFragmentBinding
import com.bink.wallet.utils.BarcodeWrapper
import com.bink.wallet.utils.observeNonNull
import com.bink.wallet.utils.toolbar.FragmentToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel

class BarcodeFragment : BaseFragment<BarcodeViewModel, BarcodeFragmentBinding>() {
    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .with(binding.toolbar).shouldDisplayBack(requireActivity())
            .build()
    }

    override val viewModel: BarcodeViewModel by viewModel()
    override val layoutRes: Int
        get() = R.layout.barcode_fragment

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        arguments?.let {
            BarcodeFragmentArgs.fromBundle(it).apply {
                viewModel.membershipPlan.value = currentMembershipPlan
                viewModel.membershipCard.value = membershipCard
                viewModel.barcode.value = BarcodeWrapper(membershipCard)

                viewModel.isBarcodeAvailable.set(!membershipCard.card?.barcode.isNullOrEmpty())
                viewModel.isCardNumberAvailable.set(!membershipCard.card?.membership_id.isNullOrEmpty())

                membershipCard.card?.let { card ->
                    if (viewModel.isCardNumberAvailable.get()) {
                        viewModel.cardNumber.set(card.membership_id)
                    }
                    if (viewModel.isBarcodeAvailable.get()) {
                        viewModel.barcodeNumber.set(card.barcode)
                    }
                }
            }
        }
        viewModel.shouldShowLabel.observeNonNull(this) {
            if (it) {
                binding.barcodeNotDisplayed.visibility = View.VISIBLE
                binding.barcodeImage.visibility = View.INVISIBLE

            } else {
                binding.barcodeNotDisplayed.visibility = View.GONE

            }
        }
        binding.viewModel = viewModel
    }

}