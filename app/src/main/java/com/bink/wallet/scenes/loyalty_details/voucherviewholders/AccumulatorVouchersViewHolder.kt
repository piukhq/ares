package com.bink.wallet.scenes.loyalty_details.voucherviewholders

import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.R
import com.bink.wallet.databinding.DetailVoucherItemBinding
import com.bink.wallet.model.response.membership_card.Voucher
import com.bink.wallet.scenes.loyalty_details.OnVoucherClickListener
import com.bink.wallet.utils.FLOAT_ONE_HUNDRED
import com.bink.wallet.utils.FLOAT_ZERO
import com.bink.wallet.utils.INT_ONE_HUNDRED
import com.bink.wallet.utils.SPACE
import com.bink.wallet.utils.ValueDisplayUtils
import com.bink.wallet.utils.enums.VoucherStates
import com.bink.wallet.utils.setTimestamp
import kotlin.math.roundToInt

class AccumulatorVouchersViewHolder(
    private val binding: DetailVoucherItemBinding,
    private val onVoucherClickListener: OnVoucherClickListener? = null
) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(thisVoucher: Voucher) {
        with(binding) {
            voucher = thisVoucher
            thisVoucher.burn?.let {
                title.text = ValueDisplayUtils.displayValue(
                    it.value,
                    it.prefix,
                    it.suffix,
                    it.currency,
                    it.type
                )
            }
            root.apply {
                this.setOnClickListener {
                    onVoucherClickListener?.invoke(thisVoucher)
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
                subtitle.text = itemView.context.getString(R.string.for_earning_prefix).plus(
                    (if (!thisVoucher.subtext.isNullOrEmpty()) thisVoucher.subtext else "").plus(
                        SPACE
                    ).plus(
                        ValueDisplayUtils.displayValue(
                            earn.target_value,
                            earn.prefix,
                            earn.suffix,
                            earn.currency
                        )
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