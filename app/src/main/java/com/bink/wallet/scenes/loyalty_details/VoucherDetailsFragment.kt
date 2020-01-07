package com.bink.wallet.scenes.loyalty_details

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.VoucherDetailsFragmentBinding
import com.bink.wallet.utils.EMPTY_STRING
import com.bink.wallet.utils.ValueDisplayUtils.displayValue
import com.bink.wallet.utils.enums.DocumentTypes
import com.bink.wallet.utils.enums.VoucherStates
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

        viewModel.voucher.value?.let { voucher ->
            with(binding.recycler) {
                layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
                val vouchers = listOf(voucher)
                adapter = LoyaltyCardDetailsVouchersAdapter(vouchers)
            }
            voucher.earn?.let { earn ->
                voucher.burn?.let { burn ->
                    if (earn.target_value != null &&
                        burn.value != null) {
                        binding.mainText.text =
                            when (voucher.state) {
                                VoucherStates.IN_PROGRESS.state -> {
                                    getString(
                                        R.string.voucher_detail_text_in_progress,
                                        displayValue(earn.target_value, burn.prefix, burn.suffix),
                                        displayValue(burn.value, burn.prefix, burn.suffix),
                                        burn.type
                                    )
                                }
                                VoucherStates.ISSUED.state -> {
                                    EMPTY_STRING
                                }
                                else -> {
                                    EMPTY_STRING
                                }
                            }
                    }
                }
            }
            var linkSet = false
            viewModel.membershipPlan.value?.account?.plan_documents?.forEach { document ->
                document.display?.let {
                    if (it.contains(DocumentTypes.VOUCHER.type)) {
                        linkSet = true
                        binding.linkText.setOnClickListener {
                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(document.url)))
                        }
                    }
                }
                if (linkSet)
                    return@forEach
            }
            if (!linkSet) {
                binding.linkText.visibility = View.GONE
            }
        }
    }
}