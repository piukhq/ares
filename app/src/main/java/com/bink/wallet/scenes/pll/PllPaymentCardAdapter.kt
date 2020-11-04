package com.bink.wallet.scenes.pll

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.R
import com.bink.wallet.model.response.payment_card.PaymentCard

typealias OnBrandHeaderClickListener = (String) -> Unit

class PllPaymentCardAdapter(
    var paymentCards: List<PaymentCard> = listOf()
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onBrandHeaderClickListener: OnBrandHeaderClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =

        PllPaymentCardViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.pll_payment_card_item,
                parent,
                false
            )
        )


    override fun getItemCount() = paymentCards.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

                paymentCards[position].let { cardWrapper ->
                    cardWrapper.let {
                        (holder as PllPaymentCardViewHolder).bindCard(
                            cardWrapper,
                            paymentCards.last() == cardWrapper
                        )
                    }
                }

    }

    fun setOnBrandHeaderClickListener(onBrandHeaderClickListener: OnBrandHeaderClickListener) {
        this.onBrandHeaderClickListener = onBrandHeaderClickListener
    }

}