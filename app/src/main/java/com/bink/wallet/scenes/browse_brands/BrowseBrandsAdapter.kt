package com.bink.wallet.scenes.browse_brands

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.R
import com.bink.wallet.databinding.BrandListItemBinding
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.utils.enums.CardType


class BrowseBrandsAdapter(
    val brands: List<MembershipPlan>,
    val itemClickListener: (MembershipPlan) -> Unit = {}
) :
    RecyclerView.Adapter<BrowseBrandsAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = BrandListItemBinding.inflate(inflater)
        binding.apply {
            root.setOnClickListener {
                item?.apply {
                    itemClickListener(this)
                }
            }
        }
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        if (position > 0) {
            if (brands[position - 1].feature_set?.card_type == 2 &&
                brands[position].feature_set?.card_type != 2
            ) {
                holder.binding.sectionTitle.text =
                    holder.binding.sectionTitle.context.getString(R.string.popular_text)
            } else {
                holder.binding.sectionTitle.visibility = View.GONE
            }
        }
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

            if (item.getCardType() == CardType.PLL) {
                binding.browseBrandsDescription.visibility = View.VISIBLE
            } else {
                binding.browseBrandsDescription.visibility = View.INVISIBLE
            }
        }
    }
}