package com.bink.wallet.scenes.loyalty_details.voucherviewholders

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.R
import com.bink.wallet.StampVoucherBinding
import com.bink.wallet.model.response.membership_card.Voucher
import com.bink.wallet.scenes.loyalty_details.OnVoucherClickListener
import com.bink.wallet.utils.enums.VoucherStates
import com.bink.wallet.utils.setTimestamp

class StampVouchersViewHolder(
    private val binding: StampVoucherBinding,
    private val onVoucherClickListener: OnVoucherClickListener? = null
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(voucher: Voucher) {
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