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

class LoyaltyCardDetailsVouchersAdapter(private val vouchers: List<Voucher>) :
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
        return LoyaltyCardDetailsVouchersViewHolder(binding)
    }

    override fun getItemCount() = vouchers.size

    override fun onBindViewHolder(holder: LoyaltyCardDetailsVouchersViewHolder, position: Int) =
        holder.bind(vouchers[position])


    class LoyaltyCardDetailsVouchersViewHolder(
        var binding: DetailVoucherItemBinding
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
            }
            binding.executePendingBindings()
            when (thisVoucher.state) {
                VoucherStates.IN_PROGRESS.state,
                VoucherStates.ISSUED.state -> {
                    if (thisVoucher.earn?.target_value != null &&
                        thisVoucher.earn.target_value != FLOAT_ZERO) {
                        if (thisVoucher.earn.value != null) {
                            binding.spentAmount.text = ValueDisplayUtils.displayValue(
                                thisVoucher.earn.value,
                                thisVoucher.burn?.prefix,
                                thisVoucher.burn?.suffix,
                                thisVoucher.burn?.currency
                            )
                        }
                        val goal = ValueDisplayUtils.displayValue(
                            thisVoucher.earn.target_value,
                            thisVoucher.burn?.prefix,
                            thisVoucher.burn?.suffix,
                            thisVoucher.burn?.currency
                        )
                        with (binding) {
                            subtitle.text = thisVoucher.subtext.plus(SPACE).plus(goal)
                            progressBar.max =
                                (thisVoucher.earn.target_value * FLOAT_ONE_HUNDRED).roundToInt()
                            progressBar.progress =
                                ((thisVoucher.earn.value
                                    ?: FLOAT_ZERO) * FLOAT_ONE_HUNDRED).roundToInt()
                            goalAmount.text = goal
                        }
                    } else {
                        hideEarnBurnValues()
                    }
                    if (thisVoucher.state == VoucherStates.ISSUED.state) {
                        fillProgressBar()
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
