package com.bink.wallet.scenes.pll

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.R
import com.bink.wallet.databinding.PllPaymentCardItemBinding
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.payment_card.PllPaymentCardWrapper
import com.bink.wallet.utils.getCardTypeFromProvider

class PllPaymentCardAdapter(
    var membershipCard: MembershipCard?,
    var paymentCards: List<PllAdapterItem> = listOf()
) :
    RecyclerView.Adapter<PllPaymentCardAdapter.PllPaymentCardViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PllPaymentCardViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<PllPaymentCardItemBinding>(
            inflater,
            R.layout.pll_payment_card_item,
            parent,
            false
        )
        return PllPaymentCardViewHolder(binding)
    }

    override fun getItemCount() = paymentCards.size

//    override fun getItemId(position: Int): Long = paymentCards[position].id

    override fun onBindViewHolder(holder: PllPaymentCardViewHolder, position: Int) {
        paymentCards[position].let { cardWrapper ->
            cardWrapper.let {
                holder.bindCard((cardWrapper as PllAdapterItem.PaymentCardWrapperItem).pllPaymentCardWrapper)
            }
        }
    }

    fun notifyChanges(newList: List<PllAdapterItem>) {
        val oldList = paymentCards
        paymentCards = newList
        DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean = true
//                oldList[oldItemPosition].paymentCard.id == newList[newItemPosition].paymentCard.id

            override fun getOldListSize(): Int = oldList.size

            override fun getNewListSize(): Int = newList.size

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                oldList[oldItemPosition] == newList[newItemPosition]


        }).dispatchUpdatesTo(this)
    }

    inner class PllPaymentCardViewHolder(val binding: PllPaymentCardItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindCard(paymentCard: PllPaymentCardWrapper) {
            binding.paymentCard = paymentCard

            with(binding.imageView) {
                val type = paymentCard.paymentCard.card?.provider?.getCardTypeFromProvider()
                if (type != null)
                    setImageResource(type.addLogo)
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

//            if (paymentCards.last() == paymentCard) {
//                binding.separator.visibility = View.GONE
//            } else {
//                binding.separator.visibility = View.VISIBLE
//            }
        }
    }

    companion object {
        private const val PAYMENT_CARD_ITEM_ID = R.layout.pll_payment_card_item
        private const val BRAND_HEADER_ITEM_ID = R.layout.modal_brand_header
        private const val TITLE_ITEM_ID = R.layout.item_pll_title
        private const val DESCRIPTION_ITEM_ID = R.layout.item_pll_description
    }
}