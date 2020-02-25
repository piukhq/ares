package com.bink.wallet.scenes.loyalty_details

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.format.DateFormat
import android.text.style.UnderlineSpan
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.VoucherDetailsFragmentBinding
import com.bink.wallet.utils.ValueDisplayUtils.displayValue
import com.bink.wallet.utils.enums.DocumentTypes
import com.bink.wallet.utils.enums.VoucherStates
import com.bink.wallet.utils.textAndShow
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        arguments?.let {
            viewModel.membershipPlan.value =
                VoucherDetailsFragmentArgs.fromBundle(it).membershipPlan
            viewModel.voucher.value =
                VoucherDetailsFragmentArgs.fromBundle(it).voucher

        }
        binding.membershipPlan = viewModel.membershipPlan.value

        viewModel.voucher.value?.let { voucher ->
            with(binding.recycler) {
                layoutManager = LinearLayoutManager(requireContext())
                val vouchers = listOf(voucher)
                adapter = LoyaltyCardDetailsVouchersAdapter(vouchers)
            }
            voucher.earn?.let { earn ->
                voucher.burn?.let { burn ->
                    if (earn.target_value != null &&
                        burn.value != null
                    ) {
                        binding.code.textAndShow(voucher.code)
                        when (voucher.state) {
                            VoucherStates.IN_PROGRESS.state -> {
                                binding.mainText.text =
                                    getString(
                                        R.string.voucher_detail_text_in_progress,
                                        displayValue(
                                            earn.target_value,
                                            earn.prefix,
                                            earn.suffix,
                                            earn.currency,
                                            null
                                        ),
                                        displayValue(
                                            burn.value,
                                            burn.prefix,
                                            burn.suffix,
                                            burn.currency,
                                            burn.type
                                        )
                                    )
                            }
                            VoucherStates.ISSUED.state -> {
                                setVoucherDetails(
                                    title = getString(
                                        R.string.voucher_detail_title_issued,
                                        displayValue(
                                            burn.value,
                                            burn.prefix,
                                            burn.suffix,
                                            burn.currency,
                                            burn.type
                                        )
                                    ),
                                    body = getString(
                                        R.string.plr_redeem_instructions,
                                        displayValue(
                                            burn.value,
                                            burn.prefix,
                                            burn.suffix,
                                            burn.currency,
                                            null
                                        )
                                    ),
                                    firstDate = voucher.date_issued?.let {
                                        getString(
                                            R.string.voucher_detail_date_issued,
                                            dateTimeFormatTransactionTime(it)
                                        )
                                    },
                                    secondDate = voucher.expiry_date?.let {
                                        getString(
                                            R.string.voucher_detail_date_expires,
                                            dateTimeFormatTransactionTime(it)
                                        )
                                    }
                                )
                                binding.code.setTextColor(
                                    resources.getColor(
                                        R.color.green_ok,
                                        null
                                    )
                                )
                            }
                            VoucherStates.EXPIRED.state -> {
                                setVoucherDetails(
                                    title = getString(
                                        R.string.voucher_detail_title_expired,
                                        displayValue(
                                            burn.value,
                                            burn.prefix,
                                            burn.suffix,
                                            burn.currency,
                                            burn.type
                                        )
                                    ),
                                    firstDate = voucher.date_issued?.let {
                                        getString(
                                            R.string.voucher_detail_date_issued,
                                            dateTimeFormatTransactionTime(it)
                                        )
                                    },
                                    secondDate = voucher.expiry_date?.let {
                                        getString(
                                            R.string.voucher_detail_date_expired,
                                            dateTimeFormatTransactionTime(it)
                                        )
                                    }
                                )
                            }
                            VoucherStates.REDEEMED.state -> {
                                setVoucherDetails(
                                    title = getString(
                                        R.string.voucher_detail_title_redeemed,
                                        displayValue(
                                            burn.value,
                                            burn.prefix,
                                            burn.suffix,
                                            burn.currency,
                                            burn.type
                                        )
                                    ),
                                    firstDate = voucher.date_issued?.let {
                                        getString(
                                            R.string.voucher_detail_date_issued,
                                            dateTimeFormatTransactionTime(it)
                                        )
                                    },
                                    secondDate = voucher.date_redeemed?.let {
                                        getString(
                                            R.string.voucher_detail_date_redeemed,
                                            dateTimeFormatTransactionTime(it)
                                        )
                                    }
                                )
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
                with(binding.linkText) {
                    val builder = SpannableString(text.toString())
                    builder.setSpan(
                        UnderlineSpan(),
                        0,
                        text.toString().length,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    text = builder
                }
            }
        }
    }

    private fun setVoucherDetails(
        title: String? = null,
        body: String? = null,
        firstDate: String? = null,
        secondDate: String? = null
    ) {
        binding.apply {
            mainTitle.textAndShow(title)
            mainText.textAndShow(body)
            dateOne.textAndShow(firstDate)
            dateTwo.textAndShow(secondDate)
        }
    }

    companion object {
        private fun dateTimeFormatTransactionTime(timeStamp: Long) =
            DateFormat.format("dd MMM yyyy HH:mm:ss", timeStamp * 1000).toString()
    }

}
