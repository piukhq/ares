package com.bink.wallet.scenes.loyalty_details

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.R
import com.bink.wallet.StampVoucherBinding
import com.bink.wallet.databinding.DetailVoucherItemBinding
import com.bink.wallet.model.response.membership_card.Voucher
import com.bink.wallet.utils.*
import com.bink.wallet.utils.enums.VoucherStates
import kotlin.math.roundToInt

class LoyaltyCardDetailsVouchersAdapter(
    private val vouchers: List<Voucher>,
    val onClickListener: (Any) -> Unit = {}
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        return when (viewType) {
            STAMP_VOUCHER -> {
                StampVouchersViewHolder(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.item_stamp_voucher,
                        parent,
                        false
                    ),
                    onClickListener
                )
            }
            else -> {
                val binding = DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.detail_voucher_item,
                    parent,
                    false
                ) as DetailVoucherItemBinding
                VouchersViewHolder(binding, onClickListener)
            }
        }
    }

    override fun getItemCount() = vouchers.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            STAMP_VOUCHER -> (holder as StampVouchersViewHolder).bind(vouchers[position])
            else -> (holder as VouchersViewHolder).bind(vouchers[position])
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (vouchers[position].earn?.type) {
            VOUCHER_TYPE_STAMP -> STAMP_VOUCHER
            else -> PROGRESS_VOUCHER
        }
    }

    class StampVouchersViewHolder(
        var binding: StampVoucherBinding,
        val onClickListener: (Any) -> Unit = {}
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(voucher: Voucher) {
            binding.root.setOnClickListener {
                onClickListener(voucher)
            }
            binding.voucher = voucher

            if (voucher.state == VoucherStates.REDEEMED.state ||
                voucher.state == VoucherStates.EXPIRED.state ||
                voucher.state == VoucherStates.NONE.state
            ) {
                voucher.expiry_date?.let {
                    binding.collectedTitle.visibility = View.GONE
                    binding.collectedAmount.visibility = View.GONE
                    displayDate(it)
                }
            }
        }

        private fun displayDate(date: Long) {
            with(binding.voucherDate) {
                visibility = View.VISIBLE
                setTimestamp(date, binding.root.context.getString(R.string.voucher_entry_date))
            }
        }
    }

    class VouchersViewHolder(
        var binding: DetailVoucherItemBinding,
        val onClickListener: (Any) -> Unit = {}
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(thisVoucher: Voucher) {
            with(binding) {
                voucher = thisVoucher
                thisVoucher.burn?.let {
                    if (it.value != null) {
                        title.text = ValueDisplayUtils.displayValue(
                            it.value,
                            it.prefix,
                            it.suffix,
                            it.currency,
                            it.type
                        )
                    }
                }
                root.apply {
                    this.setOnClickListener {
                        onClickListener(thisVoucher)
                    }
                }
            }
            binding.executePendingBindings()
            when (thisVoucher.state) {
                VoucherStates.IN_PROGRESS.state,
                VoucherStates.ISSUED.state -> {
                    thisVoucher.earn?.let { earn ->
                        if (earn.target_value != null) {
                            if (earn.value != null) {
                                binding.spentAmount.text = ValueDisplayUtils.displayValue(
                                    earn.value,
                                    earn.prefix,
                                    earn.suffix,
                                    earn.currency,
                                    forceTwoDecimals = true
                                )
                            }
                            displayForEarning(thisVoucher)
                            with(binding) {
                                progressBar.max =
                                    (earn.target_value * FLOAT_ONE_HUNDRED).roundToInt()
                                progressBar.progress =
                                    ((earn.value ?: FLOAT_ZERO) * FLOAT_ONE_HUNDRED).roundToInt()
                            }
                        } else {
                            hideEarnBurnValues()
                        }
                    } ?: run {
                        hideEarnBurnValues()
                    }
                    if (thisVoucher.state == VoucherStates.ISSUED.state) {
                        fillProgressBar()
                    }
                }
                VoucherStates.EXPIRED.state -> {
                    thisVoucher.expiry_date?.let {
                        displayDate(it)
                    }
                    displayForEarning(thisVoucher)
                    hideEarnBurnValues()
                    fillProgressBar()
                }
                VoucherStates.REDEEMED.state -> {
                    thisVoucher.date_redeemed?.let {
                        displayDate(it)
                    }
                    displayForEarning(thisVoucher)
                    hideEarnBurnValues()
                    fillProgressBar()
                }
                else -> {
                    hideEarnBurnValues()
                    fillProgressBar()
                }
            }
            setProgressDrawable(thisVoucher.state)
        }

        private fun displayForEarning(thisVoucher: Voucher) {
            thisVoucher.earn?.let { earn ->
                binding.apply {
                    subtitle.text = thisVoucher.subtext.plus(SPACE).plus(
                        ValueDisplayUtils.displayValue(
                            earn.target_value,
                            earn.prefix,
                            earn.suffix,
                            earn.currency
                        )
                    )
                    goalAmount.text =
                        ValueDisplayUtils.displayValue(
                            earn.target_value,
                            earn.prefix,
                            earn.suffix,
                            earn.currency,
                            forceTwoDecimals = true
                        )
                }
            }
        }

        private fun displayDate(date: Long) {
            with(binding.voucherDate) {
                visibility = View.VISIBLE
                setTimestamp(date, binding.root.context.getString(R.string.voucher_entry_date))
            }
        }

        private fun hideEarnBurnValues() {
            with(binding) {
                goalTitle.visibility = View.GONE
                goalAmount.visibility = View.GONE
                spentTitle.visibility = View.GONE
                spentAmount.visibility = View.GONE
            }
        }

        private fun fillProgressBar() {
            with(binding) {
                progressBar.max = INT_ONE_HUNDRED
                progressBar.progress = INT_ONE_HUNDRED
            }
        }

        private fun setProgressDrawable(state: String?) {
            with(binding.progressBar) {
                progressDrawable =
                    ContextCompat.getDrawable(
                        context,
                        when (state) {
                            VoucherStates.ISSUED.state ->
                                R.drawable.loyalty_voucher_progress_earned
                            VoucherStates.REDEEMED.state ->
                                R.drawable.loyalty_voucher_progress_redeemed
                            VoucherStates.EXPIRED.state ->
                                R.drawable.loyalty_voucher_progress_expired
                            else ->
                                R.drawable.loyalty_voucher_progress_bar
                        }
                    )
            }
        }
    }

    companion object {
        private const val PROGRESS_VOUCHER = R.layout.detail_voucher_item
        private const val STAMP_VOUCHER = R.layout.item_stamp_voucher
        private const val VOUCHER_TYPE_STAMP = "stamp"
    }
}
