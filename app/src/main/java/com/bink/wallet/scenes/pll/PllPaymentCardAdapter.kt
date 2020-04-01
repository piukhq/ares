package com.bink.wallet.scenes.pll

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.R

typealias OnBrandHeaderClickListener = (String) -> Unit

class PllPaymentCardAdapter(
    var paymentCards: List<PllAdapterItem> = listOf()
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onBrandHeaderClickListener: OnBrandHeaderClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            BRAND_HEADER_ITEM -> {
                BrandHeaderViewHolder(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.modal_brand_header,
                        parent,
                        false
                    )
                )
            }
            TITLE_ITEM -> {
                PllTitleViewHolder(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.item_pll_title,
                        parent,
                        false
                    )
                )
            }
            DESCRIPTION_ITEM -> {
                PllDescriptionViewHolder(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.item_pll_description,
                        parent,
                        false
                    )
                )
            }
            else -> {
                PllPaymentCardViewHolder(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.pll_payment_card_item,
                        parent,
                        false
                    )
                )
            }
        }

    override fun getItemCount() = paymentCards.size

    override fun getItemId(position: Int): Long = paymentCards[position].id.toLong()

    override fun getItemViewType(position: Int): Int = paymentCards[position].id

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            BRAND_HEADER_ITEM -> (holder as BrandHeaderViewHolder).bind(
                (paymentCards[position] as PllAdapterItem.PllBrandHeaderItem).membershipPlan,
                onBrandHeaderClickListener
            )
            DESCRIPTION_ITEM -> {
                (holder as PllDescriptionViewHolder).bind(
                    (paymentCards[position] as PllAdapterItem.PllDescriptionItem).planName
                )
            }
            PAYMENT_CARD_ITEM -> {
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

    fun setOnBrandHeaderClickListener(onBrandHeaderClickListener: OnBrandHeaderClickListener) {
        this.onBrandHeaderClickListener = onBrandHeaderClickListener
    }

    companion object {
        private const val PAYMENT_CARD_ITEM = R.layout.pll_payment_card_item
        private const val BRAND_HEADER_ITEM = R.layout.modal_brand_header
        private const val TITLE_ITEM = R.layout.item_pll_title
        private const val DESCRIPTION_ITEM = R.layout.item_pll_description
    }
}