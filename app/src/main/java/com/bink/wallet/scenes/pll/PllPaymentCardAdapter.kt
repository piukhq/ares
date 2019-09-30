package com.bink.wallet.scenes.pll

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.R
import com.bink.wallet.databinding.PllPaymentCardItemBinding
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.utils.isLinkedToMembershipCard

class PllPaymentCardAdapter(
    var membershipCard: MembershipCard?,
    var paymentCards: List<PllPaymentCardWrapper>? = null, var isAddJourney: Boolean?
) :
    RecyclerView.Adapter<PllPaymentCardAdapter.PllPaymentCardViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PllPaymentCardViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<PllPaymentCardItemBinding>(inflater, R.layout.pll_payment_card_item, parent, false)
        return PllPaymentCardViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return if(!paymentCards.isNullOrEmpty()){
            paymentCards?.size!!
        } else 0
    }

    override fun onBindViewHolder(holder: PllPaymentCardViewHolder, position: Int) {
        paymentCards?.get(position).let { it?.let { it1 -> holder.bindCard(it1) } }

    }

    inner class PllPaymentCardViewHolder(val binding: PllPaymentCardItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindCard(paymentCard: PllPaymentCardWrapper) {
            binding.paymentCard = paymentCard
            if(isAddJourney == false) {
                binding.toggle.isChecked =
                    paymentCard.paymentCard.isLinkedToMembershipCard(membershipCard!!)
                paymentCard.isSelected = binding.toggle.isChecked
                binding.toggle.displayCustomSwitch(paymentCard.isSelected!!)
            } else {
                binding.toggle.isChecked = true
                paymentCard.isSelected = true
                binding.toggle.displayCustomSwitch(true)
            }
            binding.toggle.setOnCheckedChangeListener { _, isChecked ->
                paymentCard.isSelected = isChecked
                binding.toggle.displayCustomSwitch(isChecked)
            }
            if (paymentCards?.last() == paymentCard) {
                binding.view.visibility = View.GONE
            }
        }
    }
}