package com.bink.wallet.scenes.payment_card_wallet

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.databinding.PaymentCardWalletItemBinding
import com.bink.wallet.model.response.payment_card.PaymentCard


class PaymentCardWalletAdapter(
    private val paymentCards: List<PaymentCard>,
    val onClickListener: (PaymentCard) -> Unit = {}
) : RecyclerView.Adapter<PaymentCardWalletAdapter.PaymentCardWalletHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentCardWalletHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = PaymentCardWalletItemBinding.inflate(inflater)
        return PaymentCardWalletHolder(
            binding
        )
    }

    override fun onBindViewHolder(holder: PaymentCardWalletHolder, position: Int) {
        holder.bind(paymentCards[position])
    }

    override fun getItemCount() = paymentCards.size

    override fun getItemId(position: Int) = position.toLong()

    inner class PaymentCardWalletHolder(val binding: PaymentCardWalletItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PaymentCard) {
            binding.paymentCard = item
            binding.executePendingBindings()
        }
    }
}
