package com.bink.wallet.scenes.pll

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.R
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.payment_card.PaymentCard

class PllPaymentCardAdapter(
    val paymentCards: MutableList<PaymentCard> = mutableListOf(),
    private val isFromAddJourney: Boolean
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var membershipCard: MembershipCard

    fun updateData(paymentCards: List<PaymentCard>, membershipCard: MembershipCard) {
        this.paymentCards.clear()
        this.paymentCards.addAll(paymentCards)
        this.membershipCard = membershipCard
        notifyDataSetChanged()

    }

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

        paymentCards[position].let { paymentCard ->
                (holder as PllPaymentCardViewHolder).bindCard(
                    paymentCard,
                    paymentCards.last() == paymentCard, isFromAddJourney, membershipCard
                )
        }

    }

}