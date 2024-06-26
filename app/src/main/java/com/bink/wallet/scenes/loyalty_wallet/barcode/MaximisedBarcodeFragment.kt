package com.bink.wallet.scenes.loyalty_wallet.barcode

import android.content.pm.ActivityInfo
import android.os.Bundle
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.FragmentMaximisedBarcodeBinding
import com.bink.wallet.utils.toolbar.FragmentToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel

class MaximisedBarcodeFragment :
    BaseFragment<MaximisedBarcodeViewModel, FragmentMaximisedBarcodeBinding>() {
    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .withId(FragmentToolbar.NO_TOOLBAR)
            .build()
    }

    override val layoutRes: Int
        get() = R.layout.fragment_maximised_barcode
    override val viewModel: MaximisedBarcodeViewModel by viewModel()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        arguments?.let {
            MaximisedBarcodeFragmentArgs.fromBundle(it).apply {
                viewModel.barcodeWrapper.value = barcode
                viewModel.membershipPlan.value = currentMembershipPlan
                binding.viewModel = viewModel
            }
        }
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        binding.cancel.setOnClickListener {
            requireActivity().onBackPressed()
            requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }
}