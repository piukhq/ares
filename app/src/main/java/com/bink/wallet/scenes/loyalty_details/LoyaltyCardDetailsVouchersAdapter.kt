package com.bink.wallet.scenes.loyalty_details

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.R
import com.bink.wallet.databinding.DetailVoucherItemBinding
import com.bink.wallet.model.response.membership_card.Voucher
import com.bink.wallet.utils.ValueDisplayUtils

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
            if (voucher.earn?.target_value != null) {
                binding.progressBar.max = voucher.earn.target_value
                if (voucher.earn.value != null &&
                    voucher.earn.value != 0) {
                    binding.progressBar.progress = voucher.earn.value
                }
            }
            binding.executePendingBindings()
        }
    }
}