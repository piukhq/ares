package com.bink.wallet.scenes.loyalty_wallet

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.R
import com.bink.wallet.data.SharedPreferenceManager
import com.bink.wallet.databinding.EmptyLoyaltyItemBinding
import com.bink.wallet.databinding.LoyaltyWalletItemBinding
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.scenes.add_auth_enrol.BaseViewHolder
import com.bink.wallet.utils.enums.CardStatus
import com.bink.wallet.utils.enums.CardType
import kotlin.properties.Delegates

class LoyaltyWalletAdapter(
    val onClickListener: (Any) -> Unit = {},
    val onRemoveListener: (Any) -> Unit = {}
) : RecyclerView.Adapter<BaseViewHolder<*>>() {

    companion object {
        private const val MEMBERSHIP_CARD = 0
        // used for join loyalty card
        private const val MEMBERSHIP_PLAN = 1
        private const val JOIN_PAYMENT = 2
    }

    var membershipCards: ArrayList<Any> by Delegates.observable(ArrayList()) { _, oldList, newList ->
        notifyChanges(oldList, newList)
    }

    var membershipPlans = ArrayList<MembershipPlan>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            MEMBERSHIP_CARD -> LoyaltyWalletViewHolder(LoyaltyWalletItemBinding.inflate(inflater))
            MEMBERSHIP_PLAN -> PlanSuggestionHolder(EmptyLoyaltyItemBinding.inflate(inflater))
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
            is MembershipCard -> MEMBERSHIP_CARD
            is MembershipPlan -> MEMBERSHIP_PLAN
            else -> JOIN_PAYMENT
        }
    }

    override fun getItemCount(): Int = membershipCards.size

    override fun getItemId(position: Int): Long = position.toLong()

    private fun notifyChanges(oldList: List<Any>, newList: List<Any>) {

        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                val currentOldItem = oldList[oldItemPosition]
                val currentNewItem = newList[newItemPosition]

                if (currentNewItem is MembershipCard && currentOldItem is MembershipCard)
                    return currentNewItem.id == currentOldItem.id

                if (currentNewItem is MembershipPlan && currentOldItem is MembershipPlan)
                    return currentNewItem.id == currentOldItem.id

                return false
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldList[oldItemPosition] == newList[newItemPosition]
            }

            override fun getOldListSize() = oldList.size

            override fun getNewListSize() = newList.size
        })

        diff.dispatchUpdatesTo(this)
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
                with(cardBinding) {
                    plan = currentMembershipPlan
                    item.plan = plan
                    mainLayout.setOnClickListener { onClickListener(item) }

                    when (item.status?.state) {
                        CardStatus.AUTHORISED.status -> {
                            cardLogin.visibility = View.GONE
                            valueWrapper.visibility = View.VISIBLE
                            val balance = item.balances?.first()
                            when (balance?.prefix != null) {
                                true ->
                                    loyaltyValue.text =
                                        balance?.prefix?.plus(balance.value)
                                else -> {
                                    loyaltyValue.text = balance?.value
                                    loyaltyValueExtra.text = balance?.suffix
                                }
                            }
                        }
                        CardStatus.PENDING.status -> {
                            valueWrapper.visibility = View.VISIBLE
                            cardLogin.visibility = View.GONE
                            loyaltyValue.text =
                                mainLayout.context.getString(R.string.card_status_pending)
                        }
                        CardStatus.FAILED.status -> {
                            valueWrapper.visibility = View.VISIBLE
                            cardLogin.visibility = View.GONE
                            loyaltyValue.text = mainLayout.context.getString(R.string.empty_string)
                        }
                        CardStatus.UNAUTHORISED.status -> {
                            valueWrapper.visibility = View.VISIBLE
                            cardLogin.visibility = View.GONE
                            loyaltyValue.text = mainLayout.context.getString(R.string.empty_string)
                        }
                    }
                    linkStatusWrapper.visibility = View.VISIBLE
                    when (item.getLinkStatus()) {
                        MembershipCard.RESPONSE_LINKED -> {
                            linkStatusText.text =
                                mainLayout.context.getString(R.string.loyalty_card_linked)
                            linkStatusImg.setImageResource(R.drawable.ic_linked)
                        }
                        MembershipCard.RESPONSE_LINK_NOW -> {
                            linkStatusText.text =
                                mainLayout.context.getString(R.string.loyalty_card_pll_link_now)
                            linkStatusImg.setImageResource(R.drawable.ic_unlinked)
                        }
                        MembershipCard.RESPONSE_PENDING -> {
                            linkStatusText.text =
                                mainLayout.context.getString(R.string.loyalty_card_pll_pending)
                            linkStatusImg.setImageResource(R.drawable.ic_unlinked)
                        }
                        MembershipCard.RESPONSE_RETRY -> {
                            linkStatusText.text =
                                mainLayout.context.getString(R.string.loyalty_card_pll_retry)
                            linkStatusImg.setImageResource(R.drawable.ic_unlinked)
                        }
                        "" -> {
                            linkStatusWrapper.visibility = View.GONE
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
                }
                joinCardMainLayout.setOnClickListener { onClickListener(item) }
            }
        }
    }
}