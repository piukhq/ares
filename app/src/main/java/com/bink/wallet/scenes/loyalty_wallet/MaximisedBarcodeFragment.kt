package com.bink.wallet.scenes.loyalty_wallet

import android.content.pm.ActivityInfo
import android.os.Bundle
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.FragmentMaximisedBarcodeBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class MaximisedBarcodeFragment: BaseFragment<MaximisedBarcodeViewModel, FragmentMaximisedBarcodeBinding>() {
    override val layoutRes: Int
        get() = R.layout.fragment_maximised_barcode
    override val viewModel: MaximisedBarcodeViewModel by viewModel()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        arguments?.let {
            MaximisedBarcodeFragmentArgs.fromBundle(it).apply {
                viewModel.barcodeWrapper.value = barcode
                viewModel.membershipPlan.value = currentMembershipPlan
                binding.viewModel = viewModel
            }
        }
        binding.close.setOnClickListener {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            activity?.onBackPressed()
        }
    }
}