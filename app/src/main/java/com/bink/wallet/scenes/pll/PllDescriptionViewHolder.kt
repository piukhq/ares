package com.bink.wallet.scenes.pll

import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.ItemPllDescriptionBinding

class PllDescriptionViewHolder(val binding: ItemPllDescriptionBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(planName: String) {
        binding.planNameCard = planName
    }
}