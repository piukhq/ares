package com.bink.wallet.scenes.pll

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.databinding.PendingCardItemBinding
import com.bink.wallet.model.response.payment_card.PaymentCard
import com.bink.wallet.utils.getCardTypeFromProvider

class PllPendingAdapter(
    val paymentCards: MutableList<PaymentCard>,
    val isFromPll: Boolean = false
) :
    RecyclerView.Adapter<PllPendingAdapter.PendingCardsViewHolder>() {

    fun updateData(paymentCards: List<PaymentCard>) {
        this.paymentCards.clear()
        this.paymentCards.addAll(paymentCards)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PendingCardsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = PendingCardItemBinding.inflate(inflater)
        return PendingCardsViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return paymentCards.size
    }

    override fun onBindViewHolder(holder: PendingCardsViewHolder, position: Int) {
        holder.bind(paymentCards[position])
    }

    inner class PendingCardsViewHolder(val binding: PendingCardItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(paymentCard: PaymentCard) {

            binding.paymentCard = paymentCard

            with(binding.imageView) {
                val type = paymentCard.card?.provider?.getCardTypeFromProvider()
                if (type != null) {
                    setImageResource(type.addLogo)
                }
            }

            if (isFromPll) binding.cardPending.visibility =
                View.VISIBLE else binding.cardPending.visibility = View.GONE

        }
    }
}