package com.bink.wallet.scenes.browse_brands

import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.BrandsSectionTitleBinding

class SectionTitleViewHolder(val binding: BrandsSectionTitleBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(@StringRes sectionTitle: Int, @StringRes sectionDescription: Int) {
        binding.sectionTitle.setText(sectionTitle)
        binding.sectionDescription.setText(sectionDescription)
    }
}