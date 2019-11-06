package com.bink.wallet.scenes.payment_card_details

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.adapters.AdapterViewBindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.databinding.LinkedCardsListItemBinding
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.model.response.payment_card.PaymentMembershipCard
import com.bink.wallet.utils.enums.CardStatus

class AvailablePllAdapter(
    private val cards: List<MembershipCard> = ArrayList(),
    private val plans: List<MembershipPlan> = ArrayList(),
    private val paymentMembershipCards: List<PaymentMembershipCard> = ArrayList(),
    private val onLinkStatusChange: (Pair<String?, Boolean>) -> Unit = {}, val onItemSelected: () -> Unit  = {}
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

            binding.itemLayout.setOnClickListener {
                currentMembershipPlan?.let { membershipPlan ->
                    currentMembershipCard?.let { membershipCard ->
                        onItemSelected(membershipPlan, membershipCard)
                    }
                }
            }

            when (currentMembershipCard?.status?.state) {
                CardStatus.AUTHORISED.status -> {
                    showToggle()
                }
                CardStatus.PENDING.status -> {
                    if (item.active_link != null &&
                        item.active_link
                    ) {
                        showPending()
                    }
                }
                CardStatus.UNAUTHORISED.status,
                CardStatus.FAILED.status -> {
                    if (item.active_link != null &&
                        item.active_link
                    ) {
                        showRetry()
                    }
                }
            }

            binding.executePendingBindings()
        }

        private fun getCardByPaymentId(item: PaymentMembershipCard) =
            cards.firstOrNull { it.id == item.id }

        private fun getPlanByCardID(currentMembershipCard: MembershipCard?) =
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
    }
}