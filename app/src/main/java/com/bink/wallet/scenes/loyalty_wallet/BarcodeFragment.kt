package com.bink.wallet.scenes.loyalty_wallet

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.BarcodeFragmentBinding
import com.bink.wallet.utils.BarcodeWrapper
import com.bink.wallet.utils.navigateIfAdded
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

    private val MAXIMISE_BARCODE_ANALYTICS_IDENTIFIER = "BarcodeView.MaximiseBarcode"

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        arguments?.let {
            BarcodeFragmentArgs.fromBundle(it).apply {
                viewModel.membershipPlan.value = currentMembershipPlan
                viewModel.membershipCard.value = membershipCard
                if (membershipCard.card?.membership_id.isNullOrEmpty()) {
                    with(binding) {
                        cardNumberLabel.visibility = View.GONE
                        cardNumber.visibility = View.GONE
                    }
                }
                viewModel.barcode.value = BarcodeWrapper(membershipCard)
                viewModel.isBarcodeAvailable.value = !membershipCard.card?.barcode.isNullOrEmpty()
            }
        }
        binding.viewModel = viewModel

        binding.buttonMaximize.setOnClickListener {
            val directions =
                viewModel.barcode.value?.let { barcode ->
                    viewModel.membershipPlan.value?.let { plan ->
                        BarcodeFragmentDirections.barcodeToMaximised(
                            plan,
                            barcode
                        )
                    }
                }

            directions?.let { _ -> findNavController().navigateIfAdded(this, directions) }

            logEvent(MAXIMISE_BARCODE_ANALYTICS_IDENTIFIER)
        }
    }
}