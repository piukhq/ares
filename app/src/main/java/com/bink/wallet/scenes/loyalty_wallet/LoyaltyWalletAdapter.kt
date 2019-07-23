package com.bink.wallet.scenes.loyalty_wallet

import android.content.Context
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.bink.wallet.R


class LoyaltyWalletAdapter : RecyclerView.Adapter<LoyaltyWalletAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return MyViewHolder(
            inflater,
            parent
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind("Number " + position)
    }

    override fun getItemCount(): Int {
        return 10
    }

    inner class MyViewHolder(inflater: LayoutInflater, parent: ViewGroup) : RecyclerView.ViewHolder(inflater.inflate(R.layout.loyalty_wallet_item, parent, false)) {

        private var text: TextView? = null
        var mainLayout: LinearLayout? = null

        init {

            mainLayout = itemView.findViewById(R.id.main_layout)
            text = itemView.findViewById(R.id.item_name)

        }

        fun bind(txt: String) {

            text?.text = txt

        }

    }
}