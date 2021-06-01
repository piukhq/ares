package com.bink.wallet.scenes.payment_card_wallet

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.R
import com.bink.wallet.databinding.EmptyLoyaltyItemBinding
import com.bink.wallet.databinding.PaymentCardWalletItemBinding
import com.bink.wallet.model.JoinCardItem
import com.bink.wallet.model.MembershipCardListWrapper
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.payment_card.PaymentCard
import com.bink.wallet.scenes.BaseViewHolder
import com.bink.wallet.utils.WalletOrderingUtil
import com.bink.wallet.utils.getCardTypeFromProvider
import java.util.*
import kotlin.collections.ArrayList
import kotlin.properties.Delegates

class PaymentCardWalletAdapter(
    var onClickListener: (Any) -> Unit = {}
) : RecyclerView.Adapter<BaseViewHolder<*>>() {

    companion object {
        private const val PAYMENT_CARD = 0
        private const val JOIN_PAYMENT = 2
    }

    var membershipCards: MutableList<MembershipCard>? = null

    var paymentCards: ArrayList<Any> by Delegates.observable(ArrayList()) { _, oldList, newList ->
        notifyChanges(oldList, newList)
    }

    private fun notifyChanges(oldList: List<Any>, newList: List<Any>) {
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                val currentOldItem = oldList[oldItemPosition]
                val currentNewItem = newList[newItemPosition]

                if (currentNewItem is PaymentCard &&
                    currentOldItem is PaymentCard
                )
                    return currentNewItem.id == currentOldItem.id
                return currentNewItem is JoinCardItem &&
                        currentOldItem is JoinCardItem
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                oldList[oldItemPosition] == newList[newItemPosition]


            override fun getOldListSize() = oldList.size

            override fun getNewListSize() = newList.size
        })

        diff.dispatchUpdatesTo(this)
    }

    override fun getItemViewType(position: Int): Int {
        return when (paymentCards[position]) {
            is PaymentCard -> PAYMENT_CARD
            else -> JOIN_PAYMENT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            PAYMENT_CARD -> {
                val binding = PaymentCardWalletItemBinding.inflate(inflater)
                PaymentCardWalletHolder(binding)
            }
            else -> {
                val binding = EmptyLoyaltyItemBinding.inflate(inflater)
                PaymentCardWalletJoinHolder(binding)
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

    fun onItemMove(fromPosition: Int?, toPosition: Int?): Boolean {
        fromPosition?.let {
            toPosition?.let {
                if (getItemViewType(toPosition) != PAYMENT_CARD) {
                    notifyItemMoved(fromPosition, toPosition)
                    return false
                }

                if (fromPosition < toPosition) {
                    for (i in fromPosition until toPosition) {
                        Collections.swap(paymentCards, i, i + 1)
                    }
                } else {
                    for (i in fromPosition downTo toPosition + 1) {
                        Collections.swap(paymentCards, i, i - 1)
                    }
                }
                notifyItemMoved(fromPosition, toPosition)
                WalletOrderingUtil.setSavedPaymentCardWallet(paymentCards)
                return true
            }
        }
        return false
    }

    inner class PaymentCardWalletHolder(val binding: PaymentCardWalletItemBinding) :
        BaseViewHolder<PaymentCard>(binding) {

        override fun bind(item: PaymentCard) {
            with(binding) {
                paymentCard = item
                membershipCards?.let {
                    membershipCardsWrapper = MembershipCardListWrapper(it)
                }
                executePendingBindings()

                item.card?.let { card ->
                    card.provider?.let { provider ->
                        paymentCardWrapper.setBackgroundResource(
                            provider.getCardTypeFromProvider().background
                        )
                    }

                    if (card.isExpired()) {
                        cardExpired.visibility = View.VISIBLE
                        linkStatus.visibility = View.GONE
                        imageStatus.visibility = View.GONE
                    } else {
                        cardExpired.visibility = View.GONE
                    }
                }

                mainPayment.setOnClickListener {
                    onClickListener(paymentCards[adapterPosition])
                }
            }
        }
    }

    inner class PaymentCardWalletJoinHolder(val binding: EmptyLoyaltyItemBinding) :
        BaseViewHolder<Any>(binding) {

        override fun bind(item: Any) {
            with(binding) {
                joinCardMainLayout.setOnClickListener {
                    onClickListener(paymentCards[adapterPosition])
                }

                joinCardDescription.text =
                    joinCardDescription.context.getString(R.string.payment_join_description)
            }
        }
    }
}
