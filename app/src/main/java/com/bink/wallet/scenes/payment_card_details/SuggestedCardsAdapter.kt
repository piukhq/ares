package com.bink.wallet.scenes.payment_card_details

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.databinding.LoyaltySuggestionBinding
import com.bink.wallet.model.response.membership_plan.MembershipPlan

class SuggestedCardsAdapter(
    private val plans: List<MembershipPlan> = ArrayList(),
    val itemClickListener: (MembershipPlan) -> Unit = {}
) : RecyclerView.Adapter<SuggestedCardsAdapter.SuggestedCardsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuggestedCardsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = LoyaltySuggestionBinding.inflate(inflater)

        return SuggestedCardsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SuggestedCardsViewHolder, position: Int) =
        plans[position].let { holder.bind(it) }

    override fun getItemCount(): Int = plans.size

    inner class SuggestedCardsViewHolder(val binding: LoyaltySuggestionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MembershipPlan) {
            binding.membershipPlan = item
            binding.buttonAddCard.setOnClickListener {
                itemClickListener(plans[adapterPosition])
            }

            binding.executePendingBindings()
        }
    }
}