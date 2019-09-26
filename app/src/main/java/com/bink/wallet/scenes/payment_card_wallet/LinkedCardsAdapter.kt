package com.bink.wallet.scenes.payment_card_wallet

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.databinding.LinkedCardsListItemBinding
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan

class LinkedCardsAdapter(
    private val cards: List<MembershipCard> = ArrayList(),
    val plans: List<MembershipPlan> = ArrayList(),
    val itemClickListener: (MembershipCard) -> Unit = {}
) : RecyclerView.Adapter<LinkedCardsAdapter.LinkedCardsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LinkedCardsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = LinkedCardsListItemBinding.inflate(inflater)

        binding.apply {
            root.setOnClickListener {
                item?.apply {
                    itemClickListener(this)
                }
            }
        }

        return LinkedCardsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LinkedCardsViewHolder, position: Int) {
        cards[position].let { holder.bind(it) }
    }

    override fun getItemCount(): Int = cards.size

    inner class LinkedCardsViewHolder(val binding: LinkedCardsListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MembershipCard) {
            val currentMembershipPlan = plans.first { it.id == item.membership_plan }
            binding.companyName.text = currentMembershipPlan.account?.company_name
            binding.item = item
            binding.executePendingBindings()
        }
    }
}