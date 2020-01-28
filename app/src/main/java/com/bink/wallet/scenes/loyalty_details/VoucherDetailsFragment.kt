package com.bink.wallet.scenes.loyalty_details

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.text.style.UnderlineSpan
import android.text.SpannableString
import android.text.Spanned
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.VoucherDetailsFragmentBinding
import com.bink.wallet.utils.EMPTY_STRING
import com.bink.wallet.utils.ValueDisplayUtils.displayValue
import com.bink.wallet.utils.enums.DocumentTypes
import com.bink.wallet.utils.enums.VoucherStates
import com.bink.wallet.utils.setTimestamp
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
                                        displayValue(earn.target_value, earn.prefix, earn.suffix, earn.currency, null),
                                        displayValue(burn.value, burn.prefix, burn.suffix, burn.currency, burn.type)
                                    )
                                }
                                VoucherStates.ISSUED.state -> {
                                    getString(R.string.plr_explain_redeeming)
                                }
                                VoucherStates.EXPIRED.state -> {
                                    getString(R.string.plr_explain_redeeming)
                                }
                                VoucherStates.REDEEMED.state -> {
                                    getString(R.string.plr_explain_redeeming)
                                }
                                else -> {
                                    EMPTY_STRING
                                }
                            }
                        when (voucher.state) {
                            VoucherStates.ISSUED.state -> {
                                with (binding) {
                                    code.text = voucher.code
                                    mainTitle.text = getString(
                                        R.string.voucher_detail_title_issued,
                                        displayValue(
                                            burn.value,
                                            burn.prefix,
                                            burn.suffix,
                                            burn.currency,
                                            burn.type
                                        )
                                    )
                                    redeemText.text = getString(
                                        R.string.plr_redeem_instructions,
                                        displayValue(
                                            burn.value,
                                            burn.prefix,
                                            burn.suffix,
                                            burn.currency,
                                            null
                                        )
                                    )
                                    dateOne.setTimestamp(
                                        voucher.date_issued!!,
                                        getString(R.string.voucher_detail_date_issued),
                                        true
                                    )
                                    dateTwo.setTimestamp(
                                        voucher.expiry_date!!,
                                        getString(R.string.voucher_detail_date_expires),
                                        true
                                    )
                                }
                            }
                            VoucherStates.EXPIRED.state -> {
                                with (binding) {
                                    mainTitle.text =
                                        getString(
                                            R.string.voucher_detail_title_expired,
                                            displayValue(burn.value, burn.prefix, burn.suffix, burn.currency, burn.type)
                                        )
                                    dateOne.setTimestamp(
                                        voucher.expiry_date!!,
                                        getString(R.string.voucher_detail_date_expired),
                                        true
                                    )
                                }
                            }
                            VoucherStates.REDEEMED.state -> {
                                with (binding) {
                                    mainTitle.text =
                                        getString(
                                            R.string.voucher_detail_title_redeemed,
                                            displayValue(burn.value, burn.prefix, burn.suffix, burn.currency, burn.type)
                                        )
                                    dateOne.setTimestamp(
                                        voucher.date_redeemed!!,
                                        getString(R.string.voucher_detail_date_redeemed),
                                        true
                                    )
                                }
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
            } else {
                with (binding.linkText) {
                    val builder = SpannableString(text.toString())
                    builder.setSpan(UnderlineSpan(), 0, text.toString().length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    text = builder
                }
            }
        }
    }
}
