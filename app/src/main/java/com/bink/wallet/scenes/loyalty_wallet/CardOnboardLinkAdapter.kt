package com.bink.wallet.scenes.loyalty_wallet

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.databinding.CardOnboardingItemBinding
import com.bink.wallet.databinding.CardOnboardingLoyaltyItemBinding
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.scenes.BaseViewHolder

class CardOnboardLinkAdapter(val onClickListener: (Any) -> Unit = {}) :
    RecyclerView.Adapter<CardOnboardLinkAdapter.GridImageViewHolder>() {

    private var plansList = mutableListOf<MembershipPlan>()

    fun setPlansData( plans:MutableList<MembershipPlan>){
        plansList = plans
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GridImageViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return GridImageViewHolder(CardOnboardingLoyaltyItemBinding.inflate(inflater))
    }

    override fun getItemCount(): Int {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: GridImageViewHolder, position: Int) {
        TODO("Not yet implemented")
    }

    class GridImageViewHolder(val binding: CardOnboardingLoyaltyItemBinding) :
        BaseViewHolder<MembershipPlan>(binding) {

        override fun bind(item: MembershipPlan) {
            TODO("Not yet implemented")
        }

    }
}