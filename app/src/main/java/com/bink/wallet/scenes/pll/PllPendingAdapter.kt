package com.bink.wallet.scenes.pll

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.databinding.PendingCardItemBinding
import com.bink.wallet.model.response.payment_card.PaymentCard
import com.bink.wallet.utils.getCardTypeFromProvider

class PllPendingAdapter(
    var paymentCards: MutableList<PaymentCard>,
    val isFromPll: Boolean = false,
    val clickListener : () -> Unit
) :
    RecyclerView.Adapter<PllPendingAdapter.PendingCardsViewHolder>() {

    fun updateData(paymentCards: MutableList<PaymentCard>) {
        this.paymentCards.clear()
        this.paymentCards.addAll(paymentCards)
        this.notifyDataSetChanged()
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
        holder.bind(paymentCards[position], paymentCards.lastIndex == position)
    }

    inner class PendingCardsViewHolder(val binding: PendingCardItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(paymentCard: PaymentCard, isLastCard: Boolean) {

            binding.paymentCard = paymentCard

            with(binding.imageView) {
                paymentCard.card?.provider?.getCardTypeFromProvider()?.let {
                    setImageResource(it.addLogo)
                }
            }

            binding.separator.visibility = if (isLastCard) View.INVISIBLE else View.VISIBLE

            binding.cardPending.visibility = if (isFromPll) View.VISIBLE else View.GONE

        }
    }
}