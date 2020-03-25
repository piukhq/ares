package com.bink.wallet.scenes.pll

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.ItemPllDescriptionBinding
import com.bink.wallet.ItemPllTitleBinding
import com.bink.wallet.R
import com.bink.wallet.databinding.ModalBrandHeaderBinding
import com.bink.wallet.databinding.PllPaymentCardItemBinding
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.utils.getCardTypeFromProvider
import com.bink.wallet.utils.loadImage

typealias OnBrandHeaderClickListener = (String) -> Unit
class PllPaymentCardAdapter(
    var paymentCards: List<PllAdapterItem> = listOf()
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onBrandHeaderClickListener: OnBrandHeaderClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            BRAND_HEADER_ITEM_ID -> {
                val inflater = LayoutInflater.from(parent.context)
                val binding = DataBindingUtil.inflate<ModalBrandHeaderBinding>(
                    inflater,
                    R.layout.modal_brand_header,
                    parent,
                    false
                )
                BrandHeaderViewHolder(binding)
            }
            TITLE_ITEM_ID -> {
                val inflater = LayoutInflater.from(parent.context)
                val binding = DataBindingUtil.inflate<ItemPllTitleBinding>(
                    inflater,
                    R.layout.item_pll_title,
                    parent,
                    false
                )
                PllTitleViewHolder(binding)
            }
            DESCRIPTION_ITEM_ID -> {
                val inflater = LayoutInflater.from(parent.context)
                val binding = DataBindingUtil.inflate<ItemPllDescriptionBinding>(
                    inflater,
                    R.layout.item_pll_description,
                    parent,
                    false
                )
                PllDescriptionViewHolder(binding)
            }
            else -> {
                val inflater = LayoutInflater.from(parent.context)
                val binding = DataBindingUtil.inflate<PllPaymentCardItemBinding>(
                    inflater,
                    R.layout.pll_payment_card_item,
                    parent,
                    false
                )
                PllPaymentCardViewHolder(binding)
            }
        }

    override fun getItemCount() = paymentCards.size

    override fun getItemId(position: Int): Long = paymentCards[position].id.toLong()

    override fun getItemViewType(position: Int): Int = paymentCards[position].id

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            BRAND_HEADER_ITEM_ID -> (holder as BrandHeaderViewHolder).bind(
                (paymentCards[position] as PllAdapterItem.PllBrandHeaderItem).membershipPlan,
                onBrandHeaderClickListener
            )
            DESCRIPTION_ITEM_ID -> {
                (holder as PllDescriptionViewHolder).bind(
                    (paymentCards[position] as PllAdapterItem.PllDescriptionItem).planName
                )
            }
            PAYMENT_CARD_ITEM_ID -> {
                paymentCards[position].let { cardWrapper ->
                    cardWrapper.let {
                        (holder as PllPaymentCardViewHolder).bindCard(
                            (cardWrapper as PllAdapterItem.PaymentCardItem),
                            paymentCards.last() == cardWrapper
                        )
                    }
                }
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

    fun setOnBrandHeaderClickListener(onBrandHeaderClickListener: OnBrandHeaderClickListener) {
        this.onBrandHeaderClickListener = onBrandHeaderClickListener
    }

    class BrandHeaderViewHolder(val binding: ModalBrandHeaderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(membershipPlan: MembershipPlan, onBrandHeaderClickListener: OnBrandHeaderClickListener?) {
            binding.brandImage.loadImage(membershipPlan)
            membershipPlan.account?.plan_name_card?.let {
                binding.loyaltyScheme.text =
                    binding.root.context.getString(R.string.loyalty_info, membershipPlan.account.plan_name_card)
            }
            binding.root.setOnClickListener {
                membershipPlan.account?.plan_description?.let {
                    onBrandHeaderClickListener?.invoke(it)
                }
            }
        }
    }

    class PllTitleViewHolder(val binding: ItemPllTitleBinding) : RecyclerView.ViewHolder(binding.root)

    class PllDescriptionViewHolder(val binding: ItemPllDescriptionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(planName: String) {
            binding.planNameCard = planName
        }
    }

    inner class PllPaymentCardViewHolder(val binding: PllPaymentCardItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindCard(paymentCard: PllAdapterItem.PaymentCardItem, isLast: Boolean) {
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

            if (isLast) {
                binding.separator.visibility = View.GONE
            } else {
                binding.separator.visibility = View.VISIBLE
            }
        }
    }

    companion object {
        private const val PAYMENT_CARD_ITEM_ID = R.layout.pll_payment_card_item
        private const val BRAND_HEADER_ITEM_ID = R.layout.modal_brand_header
        private const val TITLE_ITEM_ID = R.layout.item_pll_title
        private const val DESCRIPTION_ITEM_ID = R.layout.item_pll_description
    }
}