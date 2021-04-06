package com.bink.wallet.scenes.loyalty_wallet

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.databinding.CardOnboardingSeeStoreBinding
import com.bink.wallet.databinding.CardOnboardingSeeStoreItemBinding
import com.bink.wallet.databinding.CardOnboardingSeeStoreMoreItemsPlaceholderBinding
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.scenes.BaseViewHolder

class CardOnBoardingSeeStoreAdapter(val onClickListener: (MembershipPlan) -> Unit = {}) :
    RecyclerView.Adapter<BaseViewHolder<*>>() {

    private var plansList = listOf<MembershipPlan>()

    companion object {
        private const val SEE_STORE_ITEM = 0
        private const val PLACEHOLDER = 1
        private const val TARGET_POSITION = 9
    }

    fun setPlansData(plans: List<MembershipPlan>) {
        plansList = plans
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            SEE_STORE_ITEM -> SeeStoreViewHolder(CardOnboardingSeeStoreItemBinding.inflate(inflater))
            else -> MoreItemsPlaceHolder(
                CardOnboardingSeeStoreMoreItemsPlaceholderBinding.inflate(
                    inflater
                )
            )
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        when(holder){
            is SeeStoreViewHolder -> holder.bind(plansList[position])
        }
    }

    override fun getItemCount(): Int {
        return itemsToDisplay(plansList)
    }

    override fun getItemViewType(position: Int): Int {
        return when(shouldShowPlaceHolder(position)){
            true -> PLACEHOLDER
            else -> SEE_STORE_ITEM
        }
    }

    inner class SeeStoreViewHolder(val binding: CardOnboardingSeeStoreItemBinding) :
        BaseViewHolder<MembershipPlan>(binding) {

        override fun bind(item: MembershipPlan) {
            with(binding){
                membershipPlan = item
                root.setOnClickListener {
                    onClickListener(item)
                }
            }
        }
    }

    inner class MoreItemsPlaceHolder(val binding: CardOnboardingSeeStoreMoreItemsPlaceholderBinding) :
        BaseViewHolder<MembershipPlan>(binding) {
        override fun bind(item: MembershipPlan) {
            binding.root.setOnClickListener {
                onClickListener(item)
            }
        }
    }

    private fun itemsToDisplay(plansList: List<MembershipPlan>): Int {

        val size = plansList.size
        return when {
            size in 1..10 -> size
            size > 10 -> 10
            else -> 0

        }
    }

    private fun shouldShowPlaceHolder(position: Int): Boolean {
            val isLargerThanTen = plansList.size > 10
        return (isLargerThanTen && position == TARGET_POSITION) && (itemsToDisplay(plansList) == 10)

    }

}