package com.bink.wallet.scenes.payment_card_wallet

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.data.SharedPreferenceManager
import com.bink.wallet.databinding.PaymentCardWalletItemBinding
import com.bink.wallet.databinding.PaymentCardWalletJoinBinding
import com.bink.wallet.model.response.payment_card.PaymentCard
import com.bink.wallet.utils.getCardTypeFromProvider

class PaymentCardWalletAdapter(
    private val paymentCards: List<PaymentCard>,
    val onClickListener: (PaymentCard) -> Unit = {}
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        const val JOIN_CARD = 0
        const val PAYMENT_CARD = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        if (viewType == JOIN_CARD) {
            val binding = PaymentCardWalletJoinBinding.inflate(inflater)
            binding.apply {
                close.setOnClickListener {
                    SharedPreferenceManager.isPaymentJoinHidden = true
                    notifyDataSetChanged()
                }
            }
            return PaymentCardWalletJoinHolder(binding)
        } else {
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
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (!isJoinCard(position)) {
            (holder as PaymentCardWalletHolder).bind(
                paymentCards[
                    position - isJoinCardHiddenCount()
                ]
            )
        }
    }

    override fun getItemCount() =
        paymentCards.size +
        isJoinCardHiddenCount()

    override fun getItemId(position: Int) =
        position.toLong() +
        isJoinCardHiddenCount()

    override fun getItemViewType(position: Int): Int {
        return if (isJoinCard(position)) {
            JOIN_CARD
        } else {
            PAYMENT_CARD
        }
    }

    private fun isJoinCard(position: Int) =
        position == 0 &&
        isJoinCardHiddenCount() == 1

    private fun isJoinCardHiddenCount() =
        if (SharedPreferenceManager.isPaymentJoinHidden) {
            0
        } else {
            1
        }

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
        }
    }

    inner class PaymentCardWalletJoinHolder(val binding: PaymentCardWalletJoinBinding):
        RecyclerView.ViewHolder(binding.root)
}
