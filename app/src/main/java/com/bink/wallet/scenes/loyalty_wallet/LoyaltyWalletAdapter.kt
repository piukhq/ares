package com.bink.wallet.scenes.loyalty_wallet

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.R
import com.bink.wallet.data.SharedPreferenceManager
import com.bink.wallet.databinding.EmptyLoyaltyItemBinding
import com.bink.wallet.databinding.LoyaltyWalletItemBinding
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.scenes.add_auth_enrol.BaseViewHolder
import com.bink.wallet.utils.enums.CardStatus

class LoyaltyWalletAdapter(
    private val membershipPlans: List<MembershipPlan>,
    private val membershipCards: List<Any>,
    val onClickListener: (Any) -> Unit = {},
    val onRemoveListener: (Any) -> Unit = {}
) : RecyclerView.Adapter<BaseViewHolder<*>>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            0 -> {
                val binding = LoyaltyWalletItemBinding.inflate(inflater)
                LoyaltyWalletViewHolder(binding)
            }
            1 -> {
                val binding = EmptyLoyaltyItemBinding.inflate(inflater)
                PlanSuggestionHolder(binding)
            }
            else -> {
                val binding = EmptyLoyaltyItemBinding.inflate(inflater)
                binding.apply {
                    root.apply {
                        this.setOnClickListener {
                            onClickListener(it)
                        }
                    }
                }
                return PaymentCardWalletJoinHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        membershipCards[position].let {
            when (holder) {
                is LoyaltyWalletViewHolder -> holder.bind(it as MembershipCard)
                is PlanSuggestionHolder -> holder.bind(it as MembershipPlan)
                is PaymentCardWalletJoinHolder -> holder.bind(it)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (membershipCards[position]) {
            is MembershipCard -> 0
            is MembershipPlan -> 1
            else -> 2
        }
    }

    override fun getItemCount(): Int = membershipCards.size

    override fun getItemId(position: Int): Long = position.toLong()

    private fun getItemPosition(cardId: String): Int =
        membershipCards.indexOfFirst { card -> (card as MembershipCard).id == cardId }

    fun deleteCard(cardId: String) {
        (membershipCards as ArrayList<*>).removeAt(getItemPosition(cardId))
        notifyItemRemoved(getItemPosition(cardId))
    }

    inner class PaymentCardWalletJoinHolder(val binding: EmptyLoyaltyItemBinding) :
        BaseViewHolder<Any>(binding) {

        override fun bind(item: Any) {
            with(binding) {
                close.setOnClickListener {
                    SharedPreferenceManager.isPaymentJoinHidden = true
                    onRemoveListener(item)
                }

                joinCardDescription.text =
                    joinCardDescription.context.getString(R.string.payment_join_description)
            }
        }
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
            with(binding) {
                membershipPlan = item
                close.setOnClickListener {
                    onRemoveListener(membershipCards[adapterPosition] as MembershipPlan)
                    notifyItemRemoved(adapterPosition)
                }
                joinCardMainLayout.setOnClickListener { onClickListener(item) }
            }
        }
    }
}