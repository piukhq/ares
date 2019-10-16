package com.bink.wallet.scenes.payment_card_details

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.databinding.LinkedCardsListItemBinding
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.model.response.payment_card.PaymentMembershipCard

class LinkedCardsAdapter(
    private val cards: List<MembershipCard> = ArrayList(),
    private val plans: List<MembershipPlan> = ArrayList(),
    private val paymentMembershipCards: List<PaymentMembershipCard> = ArrayList(),
    private val onLinkStatusChange: (Pair<String?, Boolean>) -> Unit = {}
) : RecyclerView.Adapter<LinkedCardsAdapter.LinkedCardsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LinkedCardsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = LinkedCardsListItemBinding.inflate(inflater)

        return LinkedCardsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LinkedCardsViewHolder, position: Int) =
        paymentMembershipCards[position].let { holder.bind(it) }

    override fun getItemCount(): Int = paymentMembershipCards.size

    inner class LinkedCardsViewHolder(val binding: LinkedCardsListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PaymentMembershipCard) {
            val currentMembershipCard = cards.firstOrNull { it.id == item.id }
            val currentMembershipPlan =
                plans.firstOrNull { it.id == currentMembershipCard?.membership_plan }
            binding.companyName.text = currentMembershipPlan?.account?.company_name
            binding.paymentMembershipCard = item
            binding.membershipCard = currentMembershipCard
            binding.toggle.isChecked = item.active_link ?: false
            binding.toggle.displayCustomSwitch(item.active_link ?: false)

            binding.toggle.setOnCheckedChangeListener { _, isChecked ->
                onLinkStatusChange(Pair(item.id, isChecked))
                binding.toggle.displayCustomSwitch(isChecked)
            }

            binding.executePendingBindings()
        }
    }
}