package com.bink.wallet.scenes.loyalty_wallet

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.databinding.CardOnboardingLoyaltyItemBinding
import com.bink.wallet.databinding.CardOnboardingLoyaltyItemPlaceholderBinding
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.scenes.BaseViewHolder

class CardOnboardLinkAdapter(val onClickListener: (MembershipPlan) -> Unit = {}) :
    RecyclerView.Adapter<BaseViewHolder<*>>() {

    private var plansList = mutableListOf<MembershipPlan>()

    companion object {
        private const val IMAGE_GRID_VIEW = 0
        private const val PLACEHOLDER = 1
    }

    fun setPlansData(plans: MutableList<MembershipPlan>) {
        plansList = plans
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            IMAGE_GRID_VIEW -> GridImageViewHolder(CardOnboardingLoyaltyItemBinding.inflate(inflater))
            else -> ItemPlaceHolder(CardOnboardingLoyaltyItemPlaceholderBinding.inflate(inflater))
        }
    }

    override fun getItemCount(): Int {
        return itemsToDisplay(plansList)
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        when (holder) {
            is GridImageViewHolder -> holder.bind(plansList[position])
        }

    }

    inner class GridImageViewHolder(val binding: CardOnboardingLoyaltyItemBinding) :
        BaseViewHolder<MembershipPlan>(binding) {

        override fun bind(item: MembershipPlan) {
            with(binding) {
                membershipPlan = item
                root.setOnClickListener {
                    onClickListener(item)
                }
            }
        }

    }

    override fun getItemViewType(position: Int): Int {
        return when (shouldShowPlaceHolder(position)) {
            true -> PLACEHOLDER
            else -> IMAGE_GRID_VIEW
        }
    }

    inner class ItemPlaceHolder(val binding: CardOnboardingLoyaltyItemPlaceholderBinding) :
        BaseViewHolder<MembershipPlan>(binding) {
        override fun bind(item: MembershipPlan) {
            with(binding) {
                root.setOnClickListener {
                    onClickListener(item)
                }
            }
        }

    }

    private fun itemsToDisplay(plansList: MutableList<MembershipPlan>): Int {

        val size = plansList.size
        return when {
            size >= 4 || size == 3 -> 4
            size == 2 || size == 1 -> 2
            else -> 0

        }
    }

    private fun shouldShowPlaceHolder(position: Int): Boolean {
        return (position == plansList.size) && (itemsToDisplay(plansList) == 4 || itemsToDisplay(
            plansList
        ) == 2)

    }

}