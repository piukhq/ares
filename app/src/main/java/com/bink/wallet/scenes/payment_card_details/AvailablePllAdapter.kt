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
    private val currentPaymentCard: PaymentCard,
    private val plans: List<MembershipPlan> = ArrayList(),
    private val membershipCards: List<MembershipCard> = ArrayList(),
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

    inner class AvailablePllViewHolder(val binding: LinkedCardsListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MembershipCard) {
            val currentMembershipPlan = getPlanByCardId(item)
            binding.companyName.text = currentMembershipPlan?.account?.company_name
            binding.membershipCard = item

            isLinkedToPaymentCard(item)?.let {
                with(binding) {
                    toggle.isChecked = it
                    toggle.displayCustomSwitch(it)
                }
            } ?: run {
                with(binding) {
                    toggle.isChecked = false
                    toggle.displayCustomSwitch(false)
                }
            }
            binding.toggle.setOnCheckedChangeListener { _, isChecked ->
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