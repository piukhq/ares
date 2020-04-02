package com.bink.wallet.scenes.loyalty_details

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.R
import com.bink.wallet.databinding.DetailVoucherItemBinding
import com.bink.wallet.model.response.membership_card.Voucher
import com.bink.wallet.scenes.loyalty_details.voucherviewholders.AccumulatorVouchersViewHolder
import com.bink.wallet.scenes.loyalty_details.voucherviewholders.StampVouchersViewHolder
import com.bink.wallet.utils.VOUCHER_EARN_TYPE_STAMPS

typealias OnVoucherClickListener = (Voucher) -> Unit
class VouchersAdapter(
    private val vouchers: List<Voucher>
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var onVoucherClickListener: OnVoucherClickListener? = null

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
                    onVoucherClickListener
                )
            }
            else -> {
                val binding = DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.detail_voucher_item,
                    parent,
                    false
                ) as DetailVoucherItemBinding
                AccumulatorVouchersViewHolder(binding, onVoucherClickListener)
            }
        }
    }

    override fun getItemCount() = vouchers.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            STAMP_VOUCHER -> (holder as StampVouchersViewHolder).bind(vouchers[position])
            else -> (holder as AccumulatorVouchersViewHolder).bind(vouchers[position])
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (vouchers[position].earn?.type) {
            VOUCHER_EARN_TYPE_STAMPS -> STAMP_VOUCHER
            else -> PROGRESS_VOUCHER
        }
    }

    fun setOnVoucherClickListener(onVoucherClickListener: OnVoucherClickListener) {
        this.onVoucherClickListener = onVoucherClickListener
    }

    companion object {
        private const val PROGRESS_VOUCHER = R.layout.detail_voucher_item
        private const val STAMP_VOUCHER = R.layout.item_stamp_voucher
    }
}
