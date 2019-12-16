package com.bink.wallet.scenes.loyalty_details

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.R
import com.bink.wallet.databinding.DetailVoucherItemBinding
import com.bink.wallet.model.response.membership_card.Voucher
import com.bink.wallet.utils.*
import com.bink.wallet.utils.enums.VoucherStates
import kotlin.math.roundToInt

class LoyaltyCardDetailsVouchersAdapter(
    private val vouchers: List<Voucher>,
    val onClickListener: (Any) -> Unit = {}
) :
    RecyclerView.Adapter<LoyaltyCardDetailsVouchersAdapter.LoyaltyCardDetailsVouchersViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LoyaltyCardDetailsVouchersViewHolder {
        val binding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.detail_voucher_item,
            parent,
            false
        ) as DetailVoucherItemBinding
        return LoyaltyCardDetailsVouchersViewHolder(binding, onClickListener)
    }

    override fun getItemCount() = vouchers.size

    override fun onBindViewHolder(holder: LoyaltyCardDetailsVouchersViewHolder, position: Int) =
        holder.bind(vouchers[position])


    class LoyaltyCardDetailsVouchersViewHolder(
        var binding: DetailVoucherItemBinding,
        val onClickListener: (Any) -> Unit = {}
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(thisVoucher: Voucher) {
            with (binding) {
                voucher = thisVoucher
                if (thisVoucher.burn?.value != null) {
                    title.text = ValueDisplayUtils.displayValue(
                        thisVoucher.burn.value,
                        thisVoucher.burn.prefix,
                        thisVoucher.burn.suffix,
                        thisVoucher.burn.currency,
                        thisVoucher.burn.type
                    )
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
                        binding.spentAmount.text = ValueDisplayUtils.displayValue(
                            earn.value,
                            earn.prefix,
                            earn.suffix,
                            earn.currency
                        )
                        val goal = ValueDisplayUtils.displayValue(
                            earn.target_value,
                            earn.prefix,
                            earn.suffix,
                            earn.currency
                        )
                        with (binding) {
                            subtitle.text = thisVoucher.subtext.plus(SPACE).plus(goal)
                            progressBar.max =
                                (earn.target_value ?: FLOAT_ZERO)
                                    .times(FLOAT_ONE_HUNDRED).roundToInt()
                            progressBar.progress =
                                (earn.value ?: FLOAT_ZERO)
                                    .times(FLOAT_ONE_HUNDRED).roundToInt()
                            goalAmount.text = goal
                        }
                    }
                    if (thisVoucher.state == VoucherStates.ISSUED.state) {
                        fillProgressBar()
                    }
                }
                VoucherStates.REDEEMED.state -> {
                    fillProgressBar()
                    hideEarnBurnValues()
                    thisVoucher.date_redeemed?.let {
                        if (thisVoucher.date_redeemed != LONG_ZERO) {
                            binding.voucherDate.visibility = View.VISIBLE
                            binding.voucherDate.setTimestamp(
                                thisVoucher.date_redeemed,
                                binding.root.context.getString(R.string.voucher_entry_date)
                            )
                        }
                    }
                }
                VoucherStates.EXPIRED.state -> {
                    fillProgressBar()
                    hideEarnBurnValues()
                    thisVoucher.expiry_date?.let {
                        if (thisVoucher.expiry_date != LONG_ZERO) {
                            binding.voucherDate.visibility = View.VISIBLE
                            binding.voucherDate.setTimestamp(
                                thisVoucher.expiry_date,
                                binding.root.context.getString(R.string.voucher_entry_date)
                            )
                        }
                    }
                }
                else -> {
                    hideEarnBurnValues()
                    fillProgressBar()
                }
            }
            setProgressDrawable(thisVoucher.state)
        }

        private fun hideEarnBurnValues() {
            with (binding) {
                goalTitle.visibility = View.GONE
                goalAmount.visibility = View.GONE
                spentTitle.visibility = View.GONE
                spentAmount.visibility = View.GONE
            }
        }

        private fun fillProgressBar() {
            with (binding) {
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
}