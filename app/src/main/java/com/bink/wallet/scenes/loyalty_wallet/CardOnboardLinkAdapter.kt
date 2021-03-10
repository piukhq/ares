package com.bink.wallet.scenes.loyalty_wallet

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.databinding.CardOnboardingLoyaltyItemBinding
import com.bink.wallet.databinding.CardOnboardingLoyaltyItemPlaceholderBinding
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.scenes.BaseViewHolder

class CardOnboardLinkAdapter(val onClickListener: (Any) -> Unit = {}) :
    RecyclerView.Adapter<CardOnboardLinkAdapter.GridImageViewHolder>() {

    private var plansList = mutableListOf<MembershipPlan>()

    fun setPlansData(plans: MutableList<MembershipPlan>) {
        plansList = plans
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GridImageViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return GridImageViewHolder(CardOnboardingLoyaltyItemBinding.inflate(inflater))
    }

    override fun getItemCount(): Int {
        return itemsToDisplay(plansList)
    }

    override fun onBindViewHolder(holder: GridImageViewHolder, position: Int) {
        holder.bind(plansList[position])

    }

    class GridImageViewHolder(val binding: CardOnboardingLoyaltyItemBinding) :
        BaseViewHolder<MembershipPlan>(binding) {

        override fun bind(item: MembershipPlan) {
            with(binding) {
                membershipPlan = item
            }
        }

    }

    override fun getItemViewType(position: Int): Int {
        if ((position == plansList.size -1) && shouldShowPlaceHolder()){

        }
        return super.getItemViewType(position)
    }

    class placeHolder(val binding: CardOnboardingLoyaltyItemPlaceholderBinding) :
        BaseViewHolder<MembershipPlan>(binding) {
        override fun bind(item: MembershipPlan) {

        }

    }

    private fun itemsToDisplay(plansList: MutableList<MembershipPlan>): Int {

        val size = plansList.size
        return when {
            size >= 4 || size == 3 -> 4
            size == 2 || size == 1 || size == 1 -> 2
            else -> 0

        }
    }

    private fun shouldShowPlaceHolder():Boolean{
        return !(itemCount == 4 || itemCount == 3)

    }
}