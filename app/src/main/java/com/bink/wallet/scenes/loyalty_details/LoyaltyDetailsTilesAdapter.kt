package com.bink.wallet.scenes.loyalty_details

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.R
import com.bink.wallet.databinding.DetailTileItemBinding


class LoyaltyDetailsTilesAdapter(var tiles: List<String>) :
    RecyclerView.Adapter<LoyaltyDetailsTilesAdapter.LoyaltyDetailsTileViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LoyaltyDetailsTileViewHolder {
        val binding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.detail_tile_item,
            parent,
            false
        ) as DetailTileItemBinding
        return LoyaltyDetailsTileViewHolder(binding)
    }

    override fun getItemCount() = tiles.size

    override fun onBindViewHolder(holder: LoyaltyDetailsTileViewHolder, position: Int) {
        holder.bind(position, tiles)
    }

    class LoyaltyDetailsTileViewHolder(var binding: DetailTileItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int, tiles: List<String>) {
            binding.imageUrl = tiles[position]
        }
    }
}

