package com.bink.wallet.scenes.pll

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.databinding.PllPaymentCardItemBinding
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.payment_card.PaymentCard
import com.bink.wallet.utils.getCardTypeFromProvider
import com.bink.wallet.utils.isLinkedToMembershipCard

class PllPaymentCardViewHolder(val binding: PllPaymentCardItemBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bindCard(
        paymentCard: PaymentCard,
        isLast: Boolean,
        isFromAddJourney: Boolean,
        membershipCard: MembershipCard
    ) {
        binding.paymentCard = paymentCard

        with(binding.imageView) {
            val type = paymentCard.card?.provider?.getCardTypeFromProvider()
            if (type != null) {
                setImageResource(type.addLogo)
            }
        }

        with(binding.toggle) {
            setOnCheckedChangeListener { _, isChecked ->
                paymentCard.isSelected = isChecked
                displayCustomSwitch(isChecked)
            }

            isChecked = if (isFromAddJourney) true else paymentCard.isLinkedToMembershipCard(membershipCard)
            displayCustomSwitch(isChecked)
        }

        binding.separator.visibility = if (isLast) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }
}