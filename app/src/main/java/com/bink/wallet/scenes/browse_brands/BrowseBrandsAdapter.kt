package com.bink.wallet.scenes.browse_brands

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.databinding.BrandListItemBinding
import com.bink.wallet.scenes.browse_brands.model.MembershipPlan


class BrowseBrandsAdapter(private val brands: List<MembershipPlan>) :
    RecyclerView.Adapter<BrowseBrandsAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = BrandListItemBinding.inflate(inflater)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        brands[position].let { holder.bind(it) }
    }

    override fun getItemCount(): Int {
        return brands.size
    }

    class MyViewHolder(val binding: BrandListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MembershipPlan) {
            binding.item = item
            binding.executePendingBindings()
        }
    }
}