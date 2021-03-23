package com.bink.wallet.scenes.loyalty_wallet

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.databinding.CardOnboardingSeeStoreBinding
import com.bink.wallet.databinding.CardOnboardingSeeStoreMoreItemsPlaceholderBinding
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.scenes.BaseViewHolder

class CardOnBoardingSeeStoreAdapter(val onClickListener: (MembershipPlan) -> Unit = {}) : RecyclerView.Adapter<BaseViewHolder<*>>() {

    private var plansList = listOf<MembershipPlan>()

    companion object {
        private const val SEE_STORE_ITEM = 0
        private const val PLACEHOLDER = 1
    }

    fun setPlansData(plans: List<MembershipPlan>) {
        plansList = plans
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        val inflater = LayoutInflater.from(parent.context)

        return when(viewType){
            SEE_STORE_ITEM -> SeeStoreViewHolder(CardOnboardingSeeStoreBinding.inflate(inflater))
            else -> MoreItemsPlaceHolder(CardOnboardingSeeStoreMoreItemsPlaceholderBinding.inflate(inflater))
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        TODO("Not yet implemented")
    }

    override fun getItemCount(): Int {
        TODO("Not yet implemented")
    }

    inner class SeeStoreViewHolder(val binding:CardOnboardingSeeStoreBinding):BaseViewHolder<MembershipPlan>(binding) {
        override fun bind(item: MembershipPlan) {
            TODO("Not yet implemented")
        }
    }

    inner class MoreItemsPlaceHolder(val binding:CardOnboardingSeeStoreMoreItemsPlaceholderBinding):BaseViewHolder<MembershipPlan>(binding) {
        override fun bind(item: MembershipPlan) {
            TODO("Not yet implemented")
        }
    }

}