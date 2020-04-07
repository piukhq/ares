package com.bink.wallet.scenes.browse_brands

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.BrandItemBinding
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.utils.enums.CardType

class BrandsViewHolder(val binding: BrandItemBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(
        item: MembershipPlan,
        isLast: Boolean,
        onBrandItemClickListener: OnBrandItemClickListener?
    ) {
        binding.item = item

        binding.root.setOnClickListener {
            onBrandItemClickListener?.invoke(item)
        }

        binding.browseBrandsDescription.visibility =
            if (item.getCardType() == CardType.PLL) {
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