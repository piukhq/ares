package com.bink.wallet.scenes.browse_brands

import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.BrandItemBinding

class BrandsViewHolder(val binding: BrandItemBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(
        item: BrowseBrandsListItem.BrandItem,
        onBrandItemClickListener: OnBrandItemClickListener?
    ) {
        binding.item = item

        binding.root.setOnClickListener {
            onBrandItemClickListener?.invoke(item.membershipPlan)
        }
    }
}