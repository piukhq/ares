package com.bink.wallet.scenes.loyalty_wallet

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.R
import com.bink.wallet.databinding.EmptyLoyaltyItemBinding
import com.bink.wallet.databinding.LoyaltyWalletItemBinding
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.scenes.add_auth_enrol.BaseViewHolder
import com.bink.wallet.utils.enums.CardStatus


class LoyaltyWalletAdapter(
    private val membershipPlans: List<MembershipPlan>,
    private val membershipCards: List<Any>,
    val onClickListener: (MembershipCard) -> Unit = {},
    val onRemoveListener: (MembershipPlan) -> Unit = {}
) : RecyclerView.Adapter<BaseViewHolder<*>>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        val inflater = LayoutInflater.from(parent.context)

        return if (viewType == 0) {
            val binding = LoyaltyWalletItemBinding.inflate(inflater)
            LoyaltyWalletViewHolder(binding)
        } else {
            val binding = EmptyLoyaltyItemBinding.inflate(inflater)
            PlanSuggestionHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        membershipCards[position].let {
            when (holder) {
                is LoyaltyWalletViewHolder -> holder.bind(it as MembershipCard)
                is PlanSuggestionHolder -> holder.bind(it as MembershipPlan)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (membershipCards[position] is MembershipCard) 0 else 1
    }

    override fun getItemCount(): Int = membershipCards.size


    override fun getItemId(position: Int): Long = position.toLong()


//    private fun getItemPosition(cardId: String): Int =
//        membershipCards.indexOfFirst { card -> card.id == cardId }


    fun deleteCard(cardId: String) {
//        (membershipCards as ArrayList<MembershipCard>).removeAt(getItemPosition(cardId))
//        notifyItemRemoved(getItemPosition(cardId))
    }

    inner class LoyaltyWalletViewHolder(val binding: LoyaltyWalletItemBinding) :
        BaseViewHolder<MembershipCard>(binding) {

        override fun bind(item: MembershipCard) {
            val cardBinding = binding.cardItem
            if (!membershipPlans.isNullOrEmpty()) {
                val currentMembershipPlan = membershipPlans.first { it.id == item.membership_plan }
                cardBinding.plan = currentMembershipPlan

                cardBinding.mainLayout.setOnClickListener { onClickListener(item) }

                when (item.status?.state) {
                    CardStatus.AUTHORISED.status -> {
                        cardBinding.cardLogin.visibility = View.GONE
                        cardBinding.valueWrapper.visibility = View.VISIBLE
                        val balance = item.balances?.first()
                        when (balance?.prefix != null) {
                            true -> cardBinding.loyaltyValue.text =
                                balance?.prefix?.plus(balance.value)
                            else -> {
                                cardBinding.loyaltyValue.text = balance?.value
                                cardBinding.loyaltyValueExtra.text = balance?.suffix
                            }
                        }
                    }
                    CardStatus.PENDING.status -> {
                        cardBinding.valueWrapper.visibility = View.VISIBLE
                        cardBinding.cardLogin.visibility = View.GONE
                        cardBinding.loyaltyValue.text =
                            cardBinding.loyaltyValueExtra.context?.getString(R.string.card_status_pending)
                    }
                }

                when (currentMembershipPlan.feature_set?.card_type) {
                    2 -> when (item.status?.state) {
                        CardStatus.AUTHORISED.status -> cardBinding.linkStatusWrapper.visibility =
                            View.VISIBLE
                        CardStatus.UNAUTHORISED.status -> {
                            cardBinding.linkStatusWrapper.visibility = View.VISIBLE
                            cardBinding.linkStatusText.text =
                                cardBinding.linkStatusText.context.getString(R.string.link_status_cannot_link)
                            cardBinding.linkStatusImg.setImageResource(R.drawable.ic_unlinked)
                        }
                    }
                }
                with(cardBinding.cardView) {
                    setFirstColor(Color.parseColor(context.getString(R.string.default_card_second_color)))
                    setSecondColor(Color.parseColor(item.card?.colour))
                }
            }
        }
    }

    inner class PlanSuggestionHolder(val binding: EmptyLoyaltyItemBinding) :
        BaseViewHolder<MembershipPlan>(binding) {

        override fun bind(item: MembershipPlan) {
            binding.membershipPlan = item
            binding.closeButton.setOnClickListener {
                onRemoveListener(membershipPlans[adapterPosition])
                notifyItemRemoved(adapterPosition)
            }
        }
    }
}