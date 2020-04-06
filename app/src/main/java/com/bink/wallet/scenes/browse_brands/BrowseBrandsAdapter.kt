package com.bink.wallet.scenes.browse_brands

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.BrandItemBinding
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.utils.enums.CardType


class BrowseBrandsAdapter(
    private val brands: List<Pair<String?, MembershipPlan>>,
    private val splitPosition: Int,
    val itemClickListener: (MembershipPlan) -> Unit = {}
) : RecyclerView.Adapter<BrowseBrandsAdapter.BrandsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BrandsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = BrandItemBinding.inflate(inflater)
        binding.apply {
            container.setOnClickListener {
                item?.apply {
                    itemClickListener(this)
                }
            }
        }
        return BrandsViewHolder(binding)
    }

    override fun getItemCount() = brands.size

    override fun onBindViewHolder(holder: BrandsViewHolder, position: Int) {
        brands[position].let {
            holder.bind(
                it,
                position == itemCount - 1 || position == splitPosition
            )
        }
    }

    class BrandsViewHolder(val binding: BrandItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Pair<String?, MembershipPlan>, isLast: Boolean) {
            binding.browseBrandsDescription.visibility =
                if (item.second.getCardType() == CardType.PLL) {
                    View.VISIBLE
                } else {
                    View.GONE
                }

            binding.separator.visibility = if (isLast) {
                View.GONE
            } else {
                View.VISIBLE
            }

            binding.item = item.second
            binding.executePendingBindings()
        }
    }
}