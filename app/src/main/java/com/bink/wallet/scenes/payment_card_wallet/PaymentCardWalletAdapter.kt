package com.bink.wallet.scenes.payment_card_wallet

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.R
import com.bink.wallet.data.SharedPreferenceManager
import com.bink.wallet.databinding.EmptyLoyaltyItemBinding
import com.bink.wallet.databinding.PaymentCardWalletItemBinding
import com.bink.wallet.model.response.payment_card.PaymentCard
import com.bink.wallet.scenes.add_auth_enrol.BaseViewHolder
import com.bink.wallet.utils.getCardTypeFromProvider
import kotlin.properties.Delegates

class PaymentCardWalletAdapter(
    var onClickListener: (Any) -> Unit = {},
    var onRemoveListener: (Any) -> Unit = {}
) : RecyclerView.Adapter<BaseViewHolder<*>>() {

    companion object {
        private const val MEMBERSHIP_CARD = 0
        private const val JOIN_PAYMENT = 2
    }

    var paymentCards: ArrayList<Any> by Delegates.observable(ArrayList()) { _, oldList, newList ->
        notifyChanges(oldList, newList)
    }

    private fun notifyChanges(oldList: List<Any>, newList: List<Any>) {

        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                val currentOldItem = oldList[oldItemPosition]
                val currentNewItem = newList[newItemPosition]

                if (currentNewItem is PaymentCard && currentOldItem is PaymentCard)
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

    override fun getItemViewType(position: Int): Int {
        return when (paymentCards[position]) {
            is PaymentCard -> MEMBERSHIP_CARD
            else -> JOIN_PAYMENT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            MEMBERSHIP_CARD -> PaymentCardWalletHolder(PaymentCardWalletItemBinding.inflate(inflater))
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
        paymentCards[position].let {
            when (holder) {
                is PaymentCardWalletHolder -> holder.bind(it as PaymentCard)
                is PaymentCardWalletJoinHolder -> holder.bind(it)
            }
        }
    }

    override fun getItemCount() = paymentCards.size

    override fun getItemId(position: Int) = position.toLong()

    inner class PaymentCardWalletHolder(val binding: PaymentCardWalletItemBinding) :
        BaseViewHolder<PaymentCard>(binding) {

        override fun bind(item: PaymentCard) {
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

    inner class PaymentCardWalletJoinHolder(val binding: EmptyLoyaltyItemBinding) :
        BaseViewHolder<Any>(binding) {

        override fun bind(item: Any) {
            with(binding) {
                dismissBanner.setOnClickListener {
                    SharedPreferenceManager.isPaymentJoinHidden = true
                    onRemoveListener(paymentCards[adapterPosition])
                }

                joinCardDescription.text =
                    joinCardDescription.context.getString(R.string.payment_join_description)
            }
        }
    }
}
