package com.bink.wallet.scenes.loyalty_details

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.VoucherDetailsFragmentBinding
import com.bink.wallet.utils.LONG_ZERO
import com.bink.wallet.utils.ValueDisplayUtils.displayValue
import com.bink.wallet.utils.enums.DocumentTypes
import com.bink.wallet.utils.enums.VoucherStates
import com.bink.wallet.utils.toolbar.FragmentToolbar
import com.bink.wallet.utils.*
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
                    with(binding) {
                        val state: VoucherStates = voucher.state.getVoucherState()
                        if (state.code) {
                            code.text = voucher.code
                        } else {
                            code.visibility = View.GONE
                        }
                        hideOrDisplay(
                            state.title,
                            mainTitle,
                            displayValue(
                                burn.value,
                                burn.prefix,
                                burn.suffix
                            ),
                            burn.type
                        )
                        hideOrDisplay(
                            state.text,
                            mainText,
                            displayValue(
                                burn.value,
                                burn.prefix,
                                burn.suffix
                            ),
                            burn.type
                        )
                        if (state.date == null) {
                            voucherDate.visibility = View.GONE
                        }
                        when (state) {
                            VoucherStates.IN_PROGRESS -> {
                                mainText.text =
                                    getString(
                                        R.string.voucher_detail_text_in_progress,
                                        displayValue(
                                            earn.target_value,
                                            earn.prefix,
                                            earn.suffix
                                        ),
                                        displayValue(
                                            burn.value,
                                            burn.prefix,
                                            burn.suffix
                                        ),
                                        burn.type
                                    )
                            }
                            VoucherStates.ISSUED -> {
                                voucher.date_issued?.let {
                                    if (it != LONG_ZERO) {
                                        voucherDate.setFullTimestamp(
                                            it,
                                            getString(R.string.voucher_detail_date_issued)
                                        )
                                    }
                                }
                            }
                            VoucherStates.REDEEMED -> {
                                voucher.date_redeemed?.let {
                                    if (it != LONG_ZERO) {
                                        voucherDate.setFullTimestamp(
                                            it,
                                            getString(R.string.voucher_detail_date_redeemed)
                                        )
                                    }
                                }
                            }
                            VoucherStates.EXPIRED -> {
                                voucher.expiry_date?.let {
                                    if (it != LONG_ZERO) {
                                        voucherDate.setFullTimestamp(
                                            it,
                                            getString(R.string.voucher_detail_date_expired)
                                        )
                                    }
                                }
                            }
                            else -> {}
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

    private fun hideOrDisplay(display: Int?, textView: TextView, val1: String?, val2: String?, val3: String? = null) {
        if (display == null) {
            textView.visibility = View.GONE
        } else {
            textView.text = getString(display, val1, val2, val3)
        }
    }
}