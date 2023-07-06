package com.bink.wallet.scenes.browse_brands

import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.R
import com.bink.wallet.ScanItemBinding

class ScanCardViewHolder(
    val binding: ScanItemBinding,
    val onClickListener: (ScanCardType) -> Unit
) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind( isLastItem: Boolean) {
        val context = binding.root.context
        setClickListener(isLastItem)

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
    }

    private fun setClickListener(isLastItem: Boolean){
        val cardType = if (isLastItem) ScanCardType.CUSTOM_CARD else ScanCardType.LOYALTY_CARD

        binding.cvScanLoyaltyCard.setOnClickListener {
            onClickListener(cardType)
        }
    }
    enum class ScanCardType{
        LOYALTY_CARD,
        CUSTOM_CARD
    }
}
