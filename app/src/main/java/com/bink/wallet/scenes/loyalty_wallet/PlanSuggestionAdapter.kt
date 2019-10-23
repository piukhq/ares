package com.bink.wallet.scenes.loyalty_wallet

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.databinding.EmptyLoyaltyItemBinding
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan

class PlanSuggestionAdapter(
    private val membershipPlans: List<MembershipPlan>,
    private val membershipCards: List<MembershipCard>,
    val onClickListener: (MembershipCard) -> Unit = {}
) : RecyclerView.Adapter<PlanSuggestionAdapter.PlanSuggestionHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlanSuggestionHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = EmptyLoyaltyItemBinding.inflate(inflater)
        return PlanSuggestionHolder(
            binding
        )
    }

    override fun onBindViewHolder(holder: PlanSuggestionHolder, position: Int) {
        holder.bind(membershipPlans[position])
    }

    override fun getItemCount(): Int = membershipPlans.size

    override fun getItemId(position: Int): Long = position.toLong()

    private fun getItemPosition(cardId: String): Int =
        membershipCards.indexOfFirst { card -> card.id == cardId }

    fun deleteCard(cardId: String) {
        (membershipCards as ArrayList<MembershipCard>).removeAt(getItemPosition(cardId))
        notifyItemRemoved(getItemPosition(cardId))
    }

    inner class PlanSuggestionHolder(val binding: EmptyLoyaltyItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MembershipPlan) {
            binding.membershipPlan = item
        }
    }

}