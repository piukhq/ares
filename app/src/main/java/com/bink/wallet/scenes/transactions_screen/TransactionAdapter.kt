package com.bink.wallet.scenes.transactions_screen

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.databinding.TransactionItemBinding
import com.bink.wallet.model.response.membership_card.MembershipTransactions

class TransactionAdapter(
    private val transactions: List<MembershipTransactions>
) :
    RecyclerView.Adapter<TransactionAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = TransactionItemBinding.inflate(inflater)

        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        transactions[position].let { holder.bind(it) }
    }

    override fun getItemCount(): Int {
        return transactions.size
    }

    class MyViewHolder(val binding: TransactionItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MembershipTransactions) {
            binding.transaction = item
            binding.executePendingBindings()
        }
    }
}