package com.bink.wallet.scenes.transactions_screen

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.databinding.TransactionItemBinding
import com.bink.wallet.model.response.membership_card.MembershipTransactions

class TransactionAdapter(
    private val transactions: List<MembershipTransactions>
) :
    RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = TransactionItemBinding.inflate(inflater)

        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        transactions[position].let { holder.bind(it) }
    }

    override fun getItemCount(): Int {
        return transactions.size
    }

    class TransactionViewHolder(val binding: TransactionItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MembershipTransactions) {
            binding.transaction = item
            binding.executePendingBindings()
        }
    }
}