package com.bink.wallet.scenes.payment_card_details

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.databinding.LinkedCardsListItemBinding
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.model.response.payment_card.PaymentMembershipCard

class AvailablePllAdapter(
    private val cards: List<MembershipCard> = ArrayList(),
    private val plans: List<MembershipPlan> = ArrayList(),
    private val paymentMembershipCards: List<PaymentMembershipCard> = ArrayList(),
    private val onLinkStatusChange: (Pair<String?, Boolean>) -> Unit = {}
) : RecyclerView.Adapter<AvailablePllAdapter.AvailablePllViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AvailablePllViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = LinkedCardsListItemBinding.inflate(inflater)

        return AvailablePllViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AvailablePllViewHolder, position: Int) =
        paymentMembershipCards[position].let { holder.bind(it) }

    override fun getItemCount(): Int = paymentMembershipCards.size

    inner class AvailablePllViewHolder(val binding: LinkedCardsListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PaymentMembershipCard) {
            val currentMembershipCard = getCardByPaymentId(item)
            val currentMembershipPlan = getPlanByCardID(currentMembershipCard)
            binding.companyName.text = currentMembershipPlan?.account?.company_name
            binding.membershipCard = currentMembershipCard
            binding.toggle.isChecked = item.active_link ?: false
            binding.toggle.displayCustomSwitch(item.active_link ?: false)

            binding.toggle.setOnCheckedChangeListener { _, isChecked ->
                onLinkStatusChange(Pair(item.id, isChecked))
                binding.toggle.displayCustomSwitch(isChecked)
            }
            binding.executePendingBindings()
        }

        private fun getCardByPaymentId(item: PaymentMembershipCard) =
            cards.firstOrNull { it.id == item.id }

        private fun getPlanByCardID(currentMembershipCard: MembershipCard?) =
            plans.firstOrNull { it.id == currentMembershipCard?.membership_plan }
    }
}