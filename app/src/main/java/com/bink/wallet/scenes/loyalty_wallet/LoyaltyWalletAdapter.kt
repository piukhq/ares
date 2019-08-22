package com.bink.wallet.scenes.loyalty_wallet

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.CardView
import com.bink.wallet.R
import com.bink.wallet.databinding.LoyaltyWalletItemBinding
import com.bink.wallet.scenes.browse_brands.model.MembershipPlan
import com.bink.wallet.scenes.loyalty_wallet.model.MembershipCard
import com.bink.wallet.utils.enums.CardStatus
import com.bumptech.glide.Glide


class LoyaltyWalletAdapter(
    private val membershipPlans: List<MembershipPlan>,
    private val membershipCards: List<MembershipCard>,
    val itemDeleteListener: (MembershipCard) -> Unit = {}
) : RecyclerView.Adapter<LoyaltyWalletAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = LoyaltyWalletItemBinding.inflate(inflater)
        return MyViewHolder(
            binding
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(membershipCards[position])
    }

    override fun getItemCount(): Int {
        return membershipCards.size
    }

    fun getItem(position: Int) = membershipCards[position]

    inner class MyViewHolder(val binding: LoyaltyWalletItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var text: TextView? = null
        var mainLayout: LinearLayout? = null
        private var cardView: CardView? = null
        private var deleteLayout: RelativeLayout? = null
        private var barcodeLayout: RelativeLayout? = null
        private var logInImage: ImageView? = null
        private var valueWrapper: LinearLayout? = null
        private var textValue: TextView? = null
        private var textValueSuffix: TextView? = null
        private var linkStatusWrapper: LinearLayout? = null
        private var linkStatusText: TextView? = null
        private var linkStatusImage: ImageView? = null


        init {
            mainLayout = itemView.findViewById(R.id.main_layout)
            deleteLayout = itemView.findViewById(R.id.delete_layout)
            barcodeLayout = itemView.findViewById(R.id.barcode_layout)
            text = itemView.findViewById(R.id.company_name)
            cardView = itemView.findViewById(R.id.card_view)
            logInImage = itemView.findViewById(R.id.card_login)
            valueWrapper = itemView.findViewById(R.id.value_wrapper)
            textValue = itemView.findViewById(R.id.loyalty_value)
            textValueSuffix = itemView.findViewById(R.id.loyalty_value_extra)
            linkStatusWrapper = itemView.findViewById(R.id.link_status_wrapper)
            linkStatusText = itemView.findViewById(R.id.link_status_text)
            linkStatusImage = itemView.findViewById(R.id.link_status_img)
        }

        fun bind(item: MembershipCard) {
            if (!membershipPlans.isNullOrEmpty()) {
                val currentMembershipPlan = membershipPlans.first { it.id == item.membership_plan }
                text?.text = currentMembershipPlan.account?.company_name
                Glide.with(text?.context!!).load(currentMembershipPlan.images?.first { it.type == 3 }?.url)
                    .into(itemView.findViewById(R.id.company_logo))
                deleteLayout?.setOnClickListener { itemDeleteListener(item) }

                when (item.status?.state) {
                    CardStatus.AUTHORISED.status -> {
                        logInImage?.visibility = View.GONE
                        valueWrapper?.visibility = View.VISIBLE
                        val balance = item.balances?.first()!!
                        when (!balance.prefix.isNullOrEmpty()) {
                            true -> textValue?.text = balance.prefix?.plus(balance.value)
                            else -> {
                                textValue?.text = balance.value
                                textValueSuffix?.text = balance.suffix
                            }
                        }
                    }
                    CardStatus.PENDING.status -> {
                        valueWrapper?.visibility = View.VISIBLE
                        logInImage?.visibility = View.GONE
                        textValue?.text = textValue?.context?.getString(R.string.card_status_pending)
                    }
                }

                when (currentMembershipPlan.feature_set?.card_type) {
                    2 -> when (item.status?.state) {
                        CardStatus.AUTHORISED.status -> linkStatusWrapper?.visibility = View.VISIBLE
                        CardStatus.UNAUTHORISED.status -> {
                            linkStatusWrapper?.visibility = View.VISIBLE
                            linkStatusText?.text = linkStatusText?.context?.getString(R.string.link_status_cannot_link)
                            linkStatusImage?.setImageResource(R.drawable.ic_unlinked)
                        }
                    }
                }
                //TODO: Put the first color when available
                cardView?.setFirstColor(Color.parseColor("#888888"))
                cardView?.setSecondColor(Color.parseColor(item.card?.colour))
            }
        }

    }
}