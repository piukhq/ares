package com.bink.wallet.scenes.browse_brands

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.R
import com.bink.wallet.ScanItemBinding

class ScanCardViewHolder(val binding: ScanItemBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(onClickListener: View.OnClickListener?, isLastItem: Boolean) {
        val context = binding.root.context
        when (isLastItem) {
            true -> {
                binding.tvScanTitle.text = context.getString(R.string.add_custom_card_scan_title)
                binding.tvScanSubtitle.text = context.getString(R.string.add_custom_card_subtitle)
            }

            else -> {
                binding.tvScanTitle.text = context.getString(R.string.scan_loyalty_card)
                binding.tvScanSubtitle.text = context.getString(R.string.scan_loyalty_card_subtitle)
            }
        }
        onClickListener?.let {
            binding.root.setOnClickListener(it)
        }
    }
}
