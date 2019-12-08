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
import com.bink.wallet.utils.ValueDisplayUtils
import com.bink.wallet.utils.enums.VoucherStates
import kotlin.math.roundToInt

class LoyaltyCardDetailsVouchersAdapter(val vouchers: List<Voucher>) :
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


    class LoyaltyCardDetailsVouchersViewHolder(var binding: DetailVoucherItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(voucher: Voucher) {
            binding.voucher = voucher
            if (voucher.burn?.value != null) {
                binding.title.text = ValueDisplayUtils.displayValue(
                    voucher.burn.value,
                    voucher.burn.prefix,
                    voucher.burn.suffix,
                    voucher.burn.currency,
                    voucher.burn.type
                )
            }
            binding.executePendingBindings()
            when (voucher.state) {
                VoucherStates.IN_PROGRESS.state,
                VoucherStates.ISSUED.state -> {
                    if (voucher.earn?.value != null) {
                        binding.spentAmount.text = ValueDisplayUtils.displayValue(
                            voucher.earn.value,
                            voucher.burn?.prefix,
                            voucher.burn?.suffix,
                            voucher.burn?.currency
                        )
                    }
                    if (voucher.earn?.target_value != null) {
                        val goal = ValueDisplayUtils.displayValue(
                            voucher.earn.target_value,
                            voucher.burn?.prefix,
                            voucher.burn?.suffix,
                            voucher.burn?.currency
                        )
                        binding.subtitle.text = voucher.subtext.plus(" ").plus(goal)
                        binding.progressBar.max = voucher.earn.target_value.roundToInt()
                        binding.progressBar.progress = (voucher.earn.value ?: 0f).roundToInt()
                        binding.goalAmount.text = goal
                    }
                }
                else -> {
                    hideEarnBurnValues()
                    fillProgressBar(voucher.state)
                }
            }
            setProgressDrawable(voucher.state)
        }

        private fun hideEarnBurnValues() {
            binding.goalTitle.visibility = View.GONE
            binding.goalAmount.visibility = View.GONE
            binding.spentTitle.visibility = View.GONE
            binding.spentAmount.visibility = View.GONE
        }

        private fun fillProgressBar(state: String?) {
            binding.progressBar.max = 100
            binding.progressBar.progress = 100
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