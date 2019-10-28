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
import com.bink.wallet.utils.enums.CardType


class LoyaltyWalletAdapter(
    private val membershipPlans: List<MembershipPlan>,
    private val membershipCards: List<MembershipCard>,
    val onClickListener: (MembershipCard) -> Unit = {}
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

    override fun getItemCount(): Int = membershipCards.size


    override fun getItemId(position: Int): Long = position.toLong()


    private fun getItemPosition(cardId: String): Int =
        membershipCards.indexOfFirst { card -> card.id == cardId }


    fun deleteCard(cardId: String) {
        (membershipCards as ArrayList<MembershipCard>).removeAt(getItemPosition(cardId))
        notifyItemRemoved(getItemPosition(cardId))
    }

    inner class LoyaltyWalletViewHolder(val binding: LoyaltyWalletItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MembershipCard) {
            val cardBinding = binding.cardItem
            if (!membershipPlans.isNullOrEmpty()) {
                val currentMembershipPlan = membershipPlans.first { it.id == item.membership_plan }

                with(cardBinding) {
                    plan = currentMembershipPlan

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
                            loyaltyValue.text = cardBinding.loyaltyValueExtra.context?.getString(R.string.card_status_pending)
                        }
                        CardStatus.FAILED.status -> {
                            valueWrapper.visibility = View.VISIBLE
                            cardLogin.visibility = View.GONE
                            loyaltyValue.text = cardBinding.loyaltyValueExtra.context?.getString(R.string.link_status_auth_failed)
                        }
                        CardStatus.UNAUTHORISED.status -> {
                            valueWrapper.visibility = View.VISIBLE
                            cardLogin.visibility = View.GONE
                            loyaltyValue.text =
                                loyaltyValueExtra.context?.getString(R.string.link_status_auth_failed)
                        }
                    }
                    if (currentMembershipPlan.feature_set?.card_type != CardType.PLL.type) {
                        linkStatusWrapper.visibility = View.VISIBLE
                        linkStatusImg.setImageResource(R.drawable.ic_unlinked)
                        linkStatusText.text = binding.root.context.getString(R.string.link_status_cannot_link)
                    } else {
                        when (item.payment_cards?.size) {
                            0 -> {
                                linkStatusWrapper.visibility = View.GONE
                            }
                            else -> {
                                linkStatusWrapper.visibility = View.VISIBLE
                                linkStatusText.text =
                                    binding.root.context.getString(
                                        R.string.loyalty_card_linked
                                    )
                            }
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
}