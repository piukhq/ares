package com.bink.wallet.scenes.browse_brands

import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.BrandsSectionTitleBinding

class SectionTitleViewHolder(val binding: BrandsSectionTitleBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(sectionTitle: String) {
        binding.sectionTitle.text = sectionTitle
    }
}