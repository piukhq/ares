package com.bink.wallet.scenes.loyalty_wallet

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.databinding.CardOnboardingItemBinding
import com.bink.wallet.databinding.CardOnboardingLoyaltyItemBinding
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.scenes.BaseViewHolder

class CardOnboardLinkAdapter() :RecyclerView.Adapter<CardOnboardLinkAdapter.GridImageViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GridImageViewHolder {
        TODO("Not yet implemented")
    }

    override fun getItemCount(): Int {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: GridImageViewHolder, position: Int) {
        TODO("Not yet implemented")
    }

    class GridImageViewHolder(val binding : CardOnboardingLoyaltyItemBinding):
        BaseViewHolder<MembershipPlan>(binding) {

        override fun bind(item: MembershipPlan) {
            TODO("Not yet implemented")
        }

    }
}