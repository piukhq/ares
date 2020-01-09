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
import com.bink.wallet.utils.matchSeparator


class AvailablePllAdapter(
    private var currentPaymentCard: PaymentCard,
    private val plans: List<MembershipPlan> = ArrayList(),
    private var membershipCards: List<MembershipCardAdapterItem> = ArrayList(),
    private val onLinkStatusChange: (Pair<String?, Boolean>) -> Unit = {},
    private val onItemSelected: (MembershipPlan, MembershipCard) -> Unit
) : RecyclerView.Adapter<AvailablePllAdapter.AvailablePllViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AvailablePllViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = LinkedCardsListItemBinding.inflate(inflater)
        return AvailablePllViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AvailablePllViewHolder, position: Int) =
        membershipCards[position].let { holder.bind(it, membershipCards.lastIndex == position) }

    override fun getItemCount(): Int = membershipCards.size

    fun updatePaymentCard(updatedPaymentCard: PaymentCard) {
        val currentMembershipCards = membershipCards
        updatedPaymentCard.membership_cards.forEach { updatedMembershipCard ->
            currentMembershipCards.forEach { currentMembershipCard ->
                currentMembershipCard.isChangeable = true
                currentMembershipCard.membershipCard.payment_cards?.forEach { paymentCard ->
                    if (updatedMembershipCard.id == currentMembershipCard.membershipCard.id) {
                        paymentCard.active_link = updatedMembershipCard.active_link
                    }
                }
            }
        }
        currentPaymentCard = updatedPaymentCard
        membershipCards = currentMembershipCards
        notifyDataSetChanged()
    }

    fun setItemsClickableStatus(isClickable: Boolean) {
        val tempList = membershipCards
        tempList.forEach {
            it.isChangeable = isClickable
        }
        membershipCards = tempList
        notifyDataSetChanged()
    }

    inner class AvailablePllViewHolder(val binding: LinkedCardsListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MembershipCardAdapterItem, isLastItem: Boolean) {
            val currentMembershipPlan = getPlanByCardId(item.membershipCard)
            binding.companyName.text = currentMembershipPlan?.account?.company_name
            if (isLastItem) {
                binding.root.context.matchSeparator(binding.separator.id, binding.itemLayout)
            }
            binding.membershipCard = item.membershipCard
            binding.toggle.isEnabled = item.isChangeable
            binding.toggle.isChecked =
                if (isLinkedToPaymentCard(item.membershipCard) != null) isLinkedToPaymentCard(item.membershipCard)!! else false
            if (isLinkedToPaymentCard(item.membershipCard) != null) {
                binding.toggle.displayCustomSwitch(isLinkedToPaymentCard(item.membershipCard)!!)
            } else {
                binding.toggle.displayCustomSwitch(false)
            }

            binding.toggle.setOnClickListener {
                val isChecked = binding.toggle.isChecked
                onLinkStatusChange(Pair(item.membershipCard.id, isChecked))
                binding.toggle.displayCustomSwitch(isChecked)
            }

            binding.itemLayout.setOnClickListener {
                currentMembershipPlan?.let { membershipPlan ->
                    onItemSelected(membershipPlan, item.membershipCard)
                }
            }

            when (item.membershipCard.status?.state) {
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