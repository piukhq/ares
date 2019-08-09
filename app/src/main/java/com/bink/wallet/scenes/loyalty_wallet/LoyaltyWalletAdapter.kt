package com.bink.wallet.scenes.loyalty_wallet

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.R
import com.bink.wallet.scenes.loyalty_wallet.model.MembershipCard


class LoyaltyWalletAdapter(private val membershipCards : List<MembershipCard>,  val itemDeleteListener: (MembershipCard) -> Unit = {})  : RecyclerView.Adapter<LoyaltyWalletAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return MyViewHolder(
            inflater,
            parent
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(membershipCards[position])
    }

    override fun getItemCount(): Int {
        return membershipCards.size
    }

    inner class MyViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
        RecyclerView.ViewHolder(inflater.inflate(R.layout.loyalty_wallet_item, parent, false)) {

        private var text: TextView? = null
        var mainLayout: LinearLayout? = null
        private var deleteLayout: RelativeLayout? = null
        private var barcodeLayout: RelativeLayout? = null


        init {
            mainLayout = itemView.findViewById(R.id.main_layout)
            deleteLayout = itemView.findViewById(R.id.delete_layout)
            barcodeLayout = itemView.findViewById(R.id.barcode_layout)
            text = itemView.findViewById(R.id.item_name)
        }

        fun bind(item: MembershipCard) {
            text?.text = item.id
            deleteLayout?.setOnClickListener { itemDeleteListener(item) }
        }

    }
}