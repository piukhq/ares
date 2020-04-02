package com.bink.wallet.scenes.pll

import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.R
import com.bink.wallet.databinding.ModalBrandHeaderBinding
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.utils.loadImage

class BrandHeaderViewHolder(val binding: ModalBrandHeaderBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(membershipPlan: MembershipPlan, onBrandHeaderClickListener: OnBrandHeaderClickListener?) {
        binding.brandImage.loadImage(membershipPlan)
        membershipPlan.account?.plan_name_card?.let {
            binding.loyaltyScheme.text =
                binding.root.context.getString(R.string.loyalty_info, membershipPlan.account.plan_name_card)
        }
        binding.root.setOnClickListener {
            membershipPlan.account?.plan_description?.let {
                onBrandHeaderClickListener?.invoke(it)
            }
        }
    }
}