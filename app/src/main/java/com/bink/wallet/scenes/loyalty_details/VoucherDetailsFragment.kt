package com.bink.wallet.scenes.loyalty_details

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.UnderlineSpan
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.VoucherDetailsFragmentBinding
import com.bink.wallet.model.response.membership_card.Burn
import com.bink.wallet.model.response.membership_card.Earn
import com.bink.wallet.model.response.membership_plan.Content
import com.bink.wallet.utils.DateTimeUtils.Companion.dateTimeFormatTransactionTime
import com.bink.wallet.utils.ValueDisplayUtils.displayValue
import com.bink.wallet.utils.enums.DocumentTypes
import com.bink.wallet.utils.enums.DynamicContentColumn
import com.bink.wallet.utils.enums.VoucherStates
import com.bink.wallet.utils.textAndShow
import com.bink.wallet.utils.toolbar.FragmentToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel

class VoucherDetailsFragment :
    BaseFragment<VoucherDetailsViewModel, VoucherDetailsFragmentBinding>() {

    private val args by navArgs<VoucherDetailsFragmentArgs>()
    override val layoutRes: Int
        get() = R.layout.voucher_details_fragment
    override val viewModel: VoucherDetailsViewModel by viewModel()
    private val contentMap = mutableMapOf<String?, String?>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
    }

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .with(binding.toolbar)
            .shouldDisplayBack(requireActivity())
            .build()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.membershipPlan = args.membershipPlan

        setColumnAndValue(args.membershipPlan.content)
        args.voucher.let { voucher ->
            with(binding.recycler) {
                layoutManager = LinearLayoutManager(requireContext())
                val vouchers = listOf(voucher)
                adapter = VouchersAdapter(vouchers)
            }
            voucher.earn?.let { earn ->
                voucher.burn?.let { burn ->
                    if (voucher.state == VoucherStates.EXPIRED.state || voucher.state == VoucherStates.REDEEMED.state || voucher.state == VoucherStates.CANCELLED.state) {
                        binding.code.visibility = View.GONE

                    } else {
                        binding.code.textAndShow(voucher.code)

                    }
                    when (voucher.state) {
                        VoucherStates.IN_PROGRESS.state -> {
                            setInProgressVoucher(earn, burn)
                        }
                        VoucherStates.ISSUED.state -> {
                            setIssuedVoucher(earn, burn)
                            setVoucherDates(
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
                            setExpiredVoucher(earn, burn)
                            setVoucherDates(
                                voucher.date_issued?.let {
                                    getString(
                                        R.string.voucher_detail_date_issued,
                                        dateTimeFormatTransactionTime(it)
                                    )
                                },
                                voucher.expiry_date?.let {
                                    getString(
                                        R.string.voucher_detail_date_expired,
                                        dateTimeFormatTransactionTime(it)
                                    )
                                }
                            )
                        }
                        VoucherStates.REDEEMED.state -> {
                            setRedeemedVoucher(earn, burn)
                            setVoucherDates(
                                voucher.date_issued?.let {
                                    getString(
                                        R.string.voucher_detail_date_issued,
                                        dateTimeFormatTransactionTime(it)
                                    )
                                },
                                voucher.date_redeemed?.let {
                                    getString(
                                        R.string.voucher_detail_date_redeemed,
                                        dateTimeFormatTransactionTime(it)
                                    )
                                }
                            )
                        }

                        VoucherStates.CANCELLED.state -> {
                            setCancelledVoucher(earn, burn)
                            setVoucherDates(
                                voucher.date_issued?.let {
                                    getString(
                                        R.string.voucher_detail_date_issued,
                                        dateTimeFormatTransactionTime(it)
                                    )
                                },
                                voucher.expiry_date?.let {
                                    getString(
                                        R.string.voucher_detail_date_expired,
                                        dateTimeFormatTransactionTime(it)
                                    )
                                }
                            )
                        }
                    }
                }
            }

            args.membershipPlan.account?.plan_documents?.forEach { document ->
                document.display?.let {
                    if (it.contains(DocumentTypes.VOUCHER.type)) {
                        binding.linkText.apply {
                            setOnClickListener {
                                document.url?.let { url ->
                                    findNavController().navigate(
                                        VoucherDetailsFragmentDirections.actionVoucherDetailsFragmentToBinkWebFragment(
                                            url
                                        )
                                    )
                                }
                            }
                            text = SpannableString(document.name).apply {
                                setSpan(
                                    UnderlineSpan(),
                                    0,
                                    document.name?.length ?: 0,
                                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                                )
                            }
                            visibility = View.VISIBLE
                            return@forEach
                        }
                    }
                }
            }
        }
    }


    private fun setVoucherTitleAndBody(
        title: String? = null,
        body: String? = null

    ) {
        binding.mainTitle.textAndShow(title)
        binding.mainText.textAndShow(body)
    }

    private fun setVoucherDates(firstDate: String?, secondDate: String?) {
        binding.dateOne.textAndShow(firstDate)
        binding.dateTwo.textAndShow(secondDate)
    }

    private fun setInProgressVoucher(earn: Earn, burn: Burn) {
        if (earn.type == STAMP_VOUCHER_EARN_TYPE) {
            setVoucherTitleAndBody(
                getString(R.string.voucher_stamp_in_progress_title),
                contentMap[DynamicContentColumn.VOUCHER_IN_PROGRESS_DETAIL.type]
            )
        } else {
            setVoucherTitleAndBody(
                body = getString(
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
            )
        }
    }

    private fun setIssuedVoucher(earn: Earn, burn: Burn) {
        if (earn.type == STAMP_VOUCHER_EARN_TYPE) {
            setVoucherTitleAndBody(
                getString(
                    R.string.voucher_detail_title_issued,
                    displayValue(burn.value, burn.prefix, burn.suffix, burn.currency)
                ),
                contentMap[DynamicContentColumn.VOUCHER_ISSUED_DETAIL.type]
            )
        } else {
            setVoucherTitleAndBody(
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
                )
            )
        }
    }

    private fun setRedeemedVoucher(earn: Earn, burn: Burn) {
        if (earn.type == STAMP_VOUCHER_EARN_TYPE) {
            setVoucherTitleAndBody(
                getString(R.string.voucher_stamp_redeemed_title),
                contentMap[DynamicContentColumn.VOUCHER_REDEEMED_DETAIL.type]
            )
        } else {
            setVoucherTitleAndBody(
                title = getString(
                    R.string.voucher_detail_title_redeemed,
                    displayValue(
                        burn.value,
                        burn.prefix,
                        burn.suffix,
                        burn.currency,
                        burn.type
                    )
                )
            )
        }
    }

    private fun setExpiredVoucher(earn: Earn, burn: Burn) {
        if (earn.type == STAMP_VOUCHER_EARN_TYPE) {
            setVoucherTitleAndBody(
                getString(R.string.voucher_stamp_expired_title),
                contentMap[DynamicContentColumn.VOUCHER_EXPIRED_DETAIL.type]
            )
        } else {
            setVoucherTitleAndBody(
                title = getString(
                    R.string.voucher_detail_title_expired,
                    displayValue(
                        burn.value,
                        burn.prefix,
                        burn.suffix,
                        burn.currency,
                        burn.type
                    )
                )
            )
        }
    }

    private fun setCancelledVoucher(earn: Earn, burn: Burn) {
            if (earn.type == STAMP_VOUCHER_EARN_TYPE){
                setVoucherTitleAndBody(
                    getString(R.string.voucher_stamp_cancelled_title),
                    contentMap[DynamicContentColumn.VOUCHER_CANCELLED_DETAIL.type]
                )
            }
    }

    private fun setColumnAndValue(contentList: List<Content>?) {

        contentList?.let {
            for (content in it) {
                contentMap[content.column] = content.value
            }
        }
    }

    companion object {
        private const val STAMP_VOUCHER_EARN_TYPE = "stamps"
    }

}
