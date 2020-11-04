package com.bink.wallet.scenes.pll

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.databinding.PllPaymentCardItemBinding
import com.bink.wallet.model.response.payment_card.PaymentCard
import com.bink.wallet.utils.getCardTypeFromProvider

class PllPaymentCardViewHolder(val binding: PllPaymentCardItemBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bindCard(paymentCard: PaymentCard, isLast: Boolean) {
        binding.paymentCard = paymentCard

        with(binding.imageView) {
            val type = paymentCard.card?.provider?.getCardTypeFromProvider()
            if (type != null) {
                setImageResource(type.addLogo)
            }
        }

        with(binding.toggle) {
            setOnCheckedChangeListener(null)

            isChecked = paymentCard.isSelected
            displayCustomSwitch(paymentCard.isSelected)

            setOnCheckedChangeListener { _, isChecked ->
                paymentCard.isSelected = isChecked
                displayCustomSwitch(isChecked)
            }
        }

        binding.separator.visibility = if (isLast) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }
}