package com.bink.wallet.scenes.loyalty_details

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.R
import com.bink.wallet.databinding.DetailTileItemBinding
import com.bink.wallet.model.response.membership_plan.Images


class LoyaltyDetailsTilesAdapter(var tiles: List<Images>, var onClickListener: (Images) -> Unit = {}) :
    RecyclerView.Adapter<LoyaltyDetailsTilesAdapter.LoyaltyDetailsTileViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoyaltyDetailsTileViewHolder {
        val binding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.detail_tile_item,
            parent,
            false
        ) as DetailTileItemBinding
        return LoyaltyDetailsTileViewHolder(binding, onClickListener)
    }

    override fun getItemCount() = tiles.size

    override fun onBindViewHolder(holder: LoyaltyDetailsTileViewHolder, position: Int) =
        holder.bind(position, tiles)


    class LoyaltyDetailsTileViewHolder(var binding: DetailTileItemBinding, var onClickListener: (Images) -> Unit = {}) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int, tiles: List<Images>) {
            binding.imageUrl = tiles[position].url
            binding.tileItem.setOnClickListener{
                onClickListener(tiles[position])
            }
        }
    }
}

