package com.bink.wallet.scenes.payment_card_details

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.databinding.LinkedCardsListItemBinding
import com.bink.wallet.databinding.LoyaltySuggestionBinding
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.model.response.payment_card.PaymentCard
import com.bink.wallet.model.response.payment_card.PaymentMembershipCard
import com.bink.wallet.utils.enums.CardStatus

class LinkedCardsAdapter(
    private val cards: List<MembershipCard> = ArrayList(),
    private val plans: List<MembershipPlan> = ArrayList(),
    private val notLinkedPllCards: ArrayList<MembershipPlan> = ArrayList(),
    private val paymentMembershipCards: ArrayList<PaymentMembershipCard> = ArrayList(),
    private val onLinkStatusChange: (Pair<String?, Boolean>) -> Unit,
    private val onItemSelected: (MembershipPlan, MembershipCard) -> Unit,
    private val itemClickListener: (MembershipPlan) -> Unit = {}
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        const val EXISTING_CARDS = 0
        const val UNUSED_PLANS = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            EXISTING_CARDS -> {
                val binding = LinkedCardsListItemBinding.inflate(inflater)
                LinkedCardsViewHolder(binding)
            }
            else -> {
                val binding = LoyaltySuggestionBinding.inflate(inflater)
                SuggestedCardsViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            EXISTING_CARDS ->
                cards[position].let {
                    (holder as LinkedCardsViewHolder).bind(it)
                }
            UNUSED_PLANS -> {
                notLinkedPllCards[position - cards.size].let {
                    (holder as SuggestedCardsViewHolder).bind(it)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        val counts = plans.filterNot { plan ->
            cards.any { card ->
                card.membership_plan == plan.id
            }
        }
        return cards.size + counts.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (position < cards.size) {
            EXISTING_CARDS
        } else {
            UNUSED_PLANS
        }
    }

    fun updatePaymentCard(link: PaymentCard) {
        paymentMembershipCards.clear()
        paymentMembershipCards.addAll(link.membership_cards)
        notifyDataSetChanged()
    }

    inner class LinkedCardsViewHolder(val binding: LinkedCardsListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(currentMembershipCard: MembershipCard) {
            val item = getPaymentMembershipCard(currentMembershipCard.id)
            val currentMembershipPlan = getPlanByCardID(currentMembershipCard)
            binding.companyName.text = currentMembershipPlan?.account?.company_name
            binding.paymentMembershipCard = item
            binding.membershipCard = currentMembershipCard
            binding.toggle.isChecked = item.active_link ?: false
            binding.toggle.displayCustomSwitch(item.active_link ?: false)

            binding.toggle.setOnCheckedChangeListener { _, isChecked ->
                onLinkStatusChange(Pair(item.id, isChecked))
                binding.toggle.displayCustomSwitch(isChecked)
            }

            binding.itemLayout.setOnClickListener {
                currentMembershipPlan?.let { membershipPlan ->
                    onItemSelected(membershipPlan, currentMembershipCard)
                }
            }

            when (currentMembershipCard.status?.state) {
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

        private fun getPaymentMembershipCard(id: String): PaymentMembershipCard {
            return paymentMembershipCards.firstOrNull { it.id == id }
                ?: PaymentMembershipCard(id, false)
        }

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

    inner class SuggestedCardsViewHolder(val binding: LoyaltySuggestionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MembershipPlan) {
            binding.membershipPlan = item
            binding.buttonAddCard.setOnClickListener {
                itemClickListener(item)
            }

            binding.executePendingBindings()
        }
    }
}