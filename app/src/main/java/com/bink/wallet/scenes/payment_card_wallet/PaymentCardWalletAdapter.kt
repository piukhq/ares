package com.bink.wallet.scenes.payment_card_wallet

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.databinding.PaymentCardWalletItemBinding
import com.bink.wallet.model.response.payment_card.PaymentCard
import com.bink.wallet.utils.getCardTypeFromProvider

class PaymentCardWalletAdapter(
    private val paymentCards: List<PaymentCard>,
    val onClickListener: (PaymentCard) -> Unit = {}
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = PaymentCardWalletItemBinding.inflate(inflater)
        binding.apply {
            root.setOnClickListener {
                paymentCard?.apply {
                    onClickListener(this)
                }
            }
        }
        return PaymentCardWalletHolder(
            binding
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as PaymentCardWalletHolder).bind(paymentCards[position])
    }

    override fun getItemCount() = paymentCards.size

    override fun getItemId(position: Int) = position.toLong()

    inner class PaymentCardWalletHolder(val binding: PaymentCardWalletItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PaymentCard) {
            binding.paymentCard = item
            binding.executePendingBindings()
            item.card?.provider?.let {
                binding.paymentCardWrapper.setBackgroundResource(
                    it.getCardTypeFromProvider().background
                )
            }
            if (item.card!!.isExpired()) {
                binding.cardExpired.visibility = View.VISIBLE
                binding.linkStatus.visibility = View.GONE
                binding.imageStatus.visibility = View.GONE
            }
        }
    }
}
