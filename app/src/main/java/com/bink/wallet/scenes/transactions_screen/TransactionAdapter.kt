package com.bink.wallet.scenes.transactions_screen

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.R
import com.bink.wallet.databinding.TransactionItemBinding
import com.bink.wallet.model.response.membership_card.MembershipTransactions

class TransactionAdapter(
    private val transactions: List<MembershipTransactions>
) :
    RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder =
        TransactionViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.transaction_item,
                parent,
                false
            )
        )


    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(transactions[position])
    }

    override fun getItemCount(): Int {
        return transactions.size
    }

    class TransactionViewHolder(val binding: TransactionItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MembershipTransactions) {
            binding.transaction = item
        }
    }
}