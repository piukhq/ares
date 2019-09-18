package com.bink.wallet.scenes.loyalty_wallet

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.R
import com.bink.wallet.databinding.LoyaltyWalletItemBinding
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.utils.enums.CardStatus


class LoyaltyWalletAdapter(
    private val membershipPlans: List<MembershipPlan>,
    private val membershipCards: List<MembershipCard>,
    val onClickListener: (MembershipCard) -> Unit = {},
    val itemDeleteListener: (MembershipCard) -> Unit = {}
) : RecyclerView.Adapter<LoyaltyWalletAdapter.LoyaltyWalletViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoyaltyWalletViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<LoyaltyWalletItemBinding>(
            inflater,
            R.layout.loyalty_wallet_item,
            parent,
            false
        )
        return LoyaltyWalletViewHolder(
            binding
        )
    }

    override fun onBindViewHolder(holder: LoyaltyWalletViewHolder, position: Int) {
        holder.bind(membershipCards[position])
    }

    override fun getItemCount(): Int {
        return membershipCards.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    inner class LoyaltyWalletViewHolder(val binding: LoyaltyWalletItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MembershipCard) {
            val cardBinding = binding.cardItem
            if (!membershipPlans.isNullOrEmpty()) {
                val currentMembershipPlan = membershipPlans.first { it.id == item.membership_plan }
                cardBinding.plan = currentMembershipPlan

                binding.deleteLayout.setOnClickListener { itemDeleteListener(item) }
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
                                binding.root.context.getString(R.string.link_status_cannot_link)
                            cardBinding.linkStatusImg.setImageResource(R.drawable.ic_unlinked)
                        }
                    }
                }
                cardBinding.cardView.setFirstColor(Color.parseColor("#888888"))
                cardBinding.cardView.setSecondColor(Color.parseColor(item.card?.colour))
            }
        }

    }
}