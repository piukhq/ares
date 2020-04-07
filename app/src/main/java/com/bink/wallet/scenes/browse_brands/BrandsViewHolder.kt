package com.bink.wallet.scenes.browse_brands

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.BrandItemBinding
import com.bink.wallet.utils.enums.CardType

class BrandsViewHolder(val binding: BrandItemBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(
        item: BrowseBrandsListItem.BrandItem,
        isLast: Boolean,
        onBrandItemClickListener: OnBrandItemClickListener?
    ) {
        binding.item = item

        binding.root.setOnClickListener {
            onBrandItemClickListener?.invoke(item.membershipPlan)
        }

        binding.browseBrandsDescription.visibility =
            if (item.membershipPlan.getCardType() == CardType.PLL) {
                View.VISIBLE
            } else {
                View.GONE
            }

        binding.separator.visibility = if (isLast) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }
}