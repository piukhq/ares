package com.bink.wallet.scenes.loyalty_wallet

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.BarcodeFragmentBinding
import com.bink.wallet.utils.BarcodeWrapper
import com.bink.wallet.utils.navigateIfAdded
import org.koin.androidx.viewmodel.ext.android.viewModel

class BarcodeFragment : BaseFragment<BarcodeViewModel, BarcodeFragmentBinding>() {
    override val viewModel: BarcodeViewModel by viewModel()
    override val layoutRes: Int
        get() = R.layout.barcode_fragment

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        arguments?.let {
           BarcodeFragmentArgs.fromBundle(it).apply {
               viewModel.membershipPlan.value = currentMembershipPlan
               viewModel.barcode.value = BarcodeWrapper(barcode, barcodeType)
               viewModel.isBarcodeAvailable.value = !barcode.isNullOrEmpty()
           }
        }
        val directions =
            viewModel.barcode.value?.let { barcode ->
                viewModel.membershipPlan.value?.let { plan ->
                    BarcodeFragmentDirections.barcodeToMaximised(
                        plan,
                        barcode
                    )
                }
            }

        binding.viewModel = viewModel
        binding.barcodeImage.setOnClickListener {
            directions?.let { directions -> findNavController().navigateIfAdded(this, directions) }
        }
        binding.buttonMaximize.setOnClickListener {
            directions?.let { directions -> findNavController().navigateIfAdded(this, directions) }
        }
        binding.toolbar.setNavigationIcon(R.drawable.ic_back)
        binding.toolbar.setNavigationOnClickListener {
                activity?.onBackPressed()
            }
    }
}