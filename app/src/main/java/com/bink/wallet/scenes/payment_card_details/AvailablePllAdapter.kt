package com.bink.wallet.scenes.payment_card_details

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.databinding.LinkedCardsListItemBinding
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.model.response.payment_card.PaymentCard
import com.bink.wallet.utils.enums.MembershipCardStatus

class AvailablePllAdapter(
    private var currentPaymentCard: PaymentCard,
    private val plans: List<MembershipPlan> = ArrayList(),
    private var membershipCards: List<MembershipCard> = ArrayList(),
    private val onLinkStatusChange: (Pair<String?, Boolean>) -> Unit = {},
    private val onItemSelected: (MembershipPlan, MembershipCard) -> Unit
) : RecyclerView.Adapter<AvailablePllAdapter.AvailablePllViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AvailablePllViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = LinkedCardsListItemBinding.inflate(inflater)

        return AvailablePllViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AvailablePllViewHolder, position: Int) =
        membershipCards[position].let { holder.bind(it) }

    override fun getItemCount(): Int = membershipCards.size

    fun updatePaymentCard(updatedPaymentCard: PaymentCard) {
        val tempMembershipCards = membershipCards
        updatedPaymentCard.membership_cards.forEach {uPC ->
            tempMembershipCards.forEach { tempMembershipCard ->
                tempMembershipCard.payment_cards?.forEach { pC ->

                    if(uPC.id == tempMembershipCard.id) {
                        pC.active_link = uPC.active_link
                    }
                }
            }
        }
        currentPaymentCard = updatedPaymentCard
        membershipCards = tempMembershipCards
        notifyDataSetChanged()
    }

    inner class AvailablePllViewHolder(val binding: LinkedCardsListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MembershipCard) {
            val currentMembershipPlan = getPlanByCardId(item)
            binding.companyName.text = currentMembershipPlan?.account?.company_name
            binding.membershipCard = item
            binding.toggle.isChecked =
                if (isLinkedToPaymentCard(item) != null) isLinkedToPaymentCard(item)!! else false
            if (isLinkedToPaymentCard(item) != null) {
                binding.toggle.displayCustomSwitch(isLinkedToPaymentCard(item)!!)
            } else {
                binding.toggle.displayCustomSwitch(false)
            }

            binding.toggle.setOnClickListener {
                val isChecked = binding.toggle.isChecked
                onLinkStatusChange(Pair(item.id, isChecked))
                binding.toggle.displayCustomSwitch(isChecked)
            }

            binding.itemLayout.setOnClickListener {
                currentMembershipPlan?.let { membershipPlan ->
                    onItemSelected(membershipPlan, item)
                }
            }

            when (item.status?.state) {
                MembershipCardStatus.AUTHORISED.status -> {
                    showToggle()
                }
                MembershipCardStatus.PENDING.status -> {
                    showPending()
                }
                MembershipCardStatus.UNAUTHORISED.status,
                MembershipCardStatus.FAILED.status -> {
                    showRetry()
                }
            }

            binding.executePendingBindings()
        }

        private fun getPlanByCardId(currentMembershipCard: MembershipCard?) =
            plans.firstOrNull { it.id == currentMembershipCard?.membership_plan }

        private fun showToggle() {
            resetVisibility()
            binding.toggle.visibility = View.VISIBLE
        }

        private fun showPending() {
            resetVisibility()
            binding.pending.visibility = View.VISIBLE
        }

        private fun showRetry() {
            resetVisibility()
            binding.retry.visibility = View.VISIBLE
        }

        private fun resetVisibility() {
            binding.toggle.visibility = View.INVISIBLE
            binding.pending.visibility = View.GONE
            binding.retry.visibility = View.GONE
        }

        private fun isLinkedToPaymentCard(membershipCard: MembershipCard): Boolean? {
            return membershipCard.payment_cards?.findLast { paymentCard -> paymentCard.id == currentPaymentCard.id.toString() }
                ?.active_link
        }
    }
}