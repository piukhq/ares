package com.bink.wallet.scenes.loyalty_details

import android.os.Bundle
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.VoucherDetailsFragmentBinding
import com.bink.wallet.utils.toolbar.FragmentToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel

class VoucherDetailsFragment :
    BaseFragment<VoucherDetailsViewModel, VoucherDetailsFragmentBinding>() {

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .with(binding.toolbar)
            .shouldDisplayBack(requireActivity())
            .build()
    }

    override val layoutRes: Int
        get() = R.layout.voucher_details_fragment

    override val viewModel: VoucherDetailsViewModel by viewModel()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        arguments?.let {
            viewModel.membershipPlan.value =
                VoucherDetailsFragmentArgs.fromBundle(it).membershipPlan
            viewModel.voucher.value =
                VoucherDetailsFragmentArgs.fromBundle(it).voucher

        }
        binding.membershipPlan = viewModel.membershipPlan.value
        binding.executePendingBindings()
    }
}