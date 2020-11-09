package com.bink.wallet.scenes.loyalty_wallet

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.R
import com.bink.wallet.databinding.CardItemBinding
import com.bink.wallet.databinding.EmptyLoyaltyItemBinding
import com.bink.wallet.databinding.LoyaltyWalletItemBinding
import com.bink.wallet.model.BannerDisplay
import com.bink.wallet.model.JoinCardItem
import com.bink.wallet.model.LoyaltyWalletItem
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.model.response.payment_card.PaymentCard
import com.bink.wallet.scenes.BaseViewHolder
import com.bink.wallet.utils.ColorUtil
import com.bink.wallet.utils.VOUCHER_EARN_TYPE_STAMPS
import com.bink.wallet.utils.bindings.setVoucherCollectedProgress
import com.bink.wallet.utils.displayVoucherEarnAndTarget
import com.bink.wallet.utils.enums.MembershipCardStatus
import com.bink.wallet.utils.enums.VoucherStates
import kotlin.properties.Delegates

class LoyaltyWalletAdapter(
    val onClickListener: (Any) -> Unit = {},
    val onRemoveListener: (Any) -> Unit = {}
) : RecyclerView.Adapter<BaseViewHolder<*>>() {

    companion object {
        private const val MEMBERSHIP_CARD = 0

        // used for join loyalty card
        private const val JOIN_PLAN = 1
        private const val JOIN_PAYMENT = 2
    }

    var membershipCards: ArrayList<Any> by Delegates.observable(ArrayList()) { _, oldList, newList ->
        notifyChanges(oldList, newList)
    }

    var membershipPlans = ArrayList<MembershipPlan>()

    var paymentCards: MutableList<PaymentCard>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            MEMBERSHIP_CARD -> LoyaltyWalletViewHolder(LoyaltyWalletItemBinding.inflate(inflater))
            JOIN_PLAN -> PlanSuggestionHolder(EmptyLoyaltyItemBinding.inflate(inflater))
            else -> {
                val binding = EmptyLoyaltyItemBinding.inflate(inflater)
                binding.apply {
                    root.apply {
                        this.setOnClickListener {
                            onClickListener(it)
                        }
                    }
                }
                return PaymentCardWalletJoinHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        membershipCards[position].let {
            when (holder) {
                is LoyaltyWalletViewHolder -> holder.bind(it as MembershipCard)
                is PlanSuggestionHolder -> holder.bind(it as MembershipPlan)
                is PaymentCardWalletJoinHolder -> holder.bind(it)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (membershipCards[position]) {
            is MembershipCard -> MEMBERSHIP_CARD
            is MembershipPlan -> JOIN_PLAN
            else -> JOIN_PAYMENT
        }
    }

    override fun getItemCount(): Int = membershipCards.size

    override fun getItemId(position: Int): Long =
        when (val walletItem = membershipCards[position]) {
            is MembershipCard -> walletItem.id.toLong()
            is MembershipPlan -> walletItem.id.toLong()
            else -> position.toLong()
        }

    fun deleteBannerDisplayById(id: String) {
        val tempMembershipCards = membershipCards.toMutableList()
        var indexToDelete = -1
        var currentId = ""
        tempMembershipCards.forEachIndexed { index, card ->
            when (card) {
                is MembershipCard -> currentId = card.id
                is MembershipPlan -> currentId = card.id
                is JoinCardItem -> currentId = card.id
                is BannerDisplay -> currentId = card.id
            }
            if (currentId == id) {
                indexToDelete = index
            }
        }
        if (indexToDelete != -1) {
            tempMembershipCards.removeAt(indexToDelete)
            membershipCards = ArrayList(tempMembershipCards)
            notifyDataSetChanged()
        }
    }

    private fun notifyChanges(oldList: List<Any>, newList: List<Any>) {

        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                val currentOldItem = oldList[oldItemPosition]
                val currentNewItem = newList[newItemPosition]

                if (currentNewItem is MembershipCard &&
                    currentOldItem is MembershipCard
                )
                    return currentNewItem.id == currentOldItem.id

                if (currentNewItem is MembershipPlan &&
                    currentOldItem is MembershipPlan
                )
                    return currentNewItem.id == currentOldItem.id

                return currentNewItem is JoinCardItem &&
                        currentOldItem is JoinCardItem

            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                oldList[oldItemPosition] == newList[newItemPosition]


            override fun getOldListSize() = oldList.size

            override fun getNewListSize() = newList.size
        })

        diff.dispatchUpdatesTo(this)
    }

    inner class PaymentCardWalletJoinHolder(val binding: EmptyLoyaltyItemBinding) :
        BaseViewHolder<Any>(binding) {

        override fun bind(item: Any) {
            with(binding) {
                dismissBanner.setOnClickListener {
                    onRemoveListener(item)
                }
                joinCardDescription.text =
                    joinCardDescription.context.getString(R.string.payment_join_description)
            }
        }
    }

    inner class LoyaltyWalletViewHolder(val binding: LoyaltyWalletItemBinding) :
        BaseViewHolder<MembershipCard>(binding) {

        override fun bind(item: MembershipCard) {
            val cardBinding = binding.cardItem
            if (!membershipPlans.isNullOrEmpty()) {

                val currentMembershipPlan =
                    membershipPlans.firstOrNull { it.id == item.membership_plan }

                currentMembershipPlan?.let { membershipPlan ->
                    paymentCards?.let {
                        val loyaltyItem = LoyaltyWalletItem(item, membershipPlan, it)
                        bindCardToLoyaltyItem(loyaltyItem, binding)
                    }

                    membershipPlan.card?.let {  membershipPlanCard ->
                        item.card?.let { 
                            it.secondary_colour = membershipPlanCard.secondary_colour
                        }
                    }

                    bindVouchersToDisplay(cardBinding, membershipPlan, item)
                }
            }
            with(cardBinding.cardView) {
                setFirstColor(Color.parseColor(item.card?.getSecondaryColor()))
                setSecondColor(Color.parseColor(item.card?.colour))
            }

            bindSecondaryColorChanges(cardBinding, Color.parseColor(item.card?.colour))
        }

        private fun LoyaltyWalletAdapter.bindVouchersToDisplay(
            cardBinding: CardItemBinding,
            currentMembershipPlan: MembershipPlan,
            item: MembershipCard
        ) {
            with(cardBinding) {
                plan = currentMembershipPlan
                item.plan = plan
                mainLayout.setOnClickListener { onClickListener(item) }

                if (item.status?.state == MembershipCardStatus.AUTHORISED.status) {
                    cardLogin.visibility = View.GONE
                    valueWrapper.visibility = View.VISIBLE
                    if (!item.vouchers.isNullOrEmpty()) {
                        item.vouchers?.firstOrNull { it.state == VoucherStates.IN_PROGRESS.state }
                            ?.let { voucher ->
                                if (voucher.earn?.type == VOUCHER_EARN_TYPE_STAMPS) {
                                    loyaltyValue.setVoucherCollectedProgress(voucher.earn)
                                } else {
                                    loyaltyValue.text =
                                        root.context.displayVoucherEarnAndTarget(voucher)
                                }
                                loyaltyValueExtra.text =
                                    if (voucher.earn?.type == VOUCHER_EARN_TYPE_STAMPS) root.context.getString(
                                        R.string.earned
                                    ) else root.context.getString(
                                        R.string.spent
                                    )

                            }
                    } else if (!item.balances.isNullOrEmpty()) {
                        val balance = item.balances?.first()
                        when (balance?.prefix != null) {
                            true ->
                                loyaltyValue.text =
                                    balance?.prefix?.plus(balance.value)
                            else -> {
                                loyaltyValue.text = balance?.value
                                loyaltyValueExtra.text = balance?.suffix
                            }
                        }
                    }
                }
            }
        }

        private fun bindCardToLoyaltyItem(
            loyaltyItem: LoyaltyWalletItem,
            binding: LoyaltyWalletItemBinding
        ) {
            with(binding.cardItem) {
                loyaltyItem.apply {
                    loyaltyValue.text =
                        if (shouldShowPoints()) {
                            retrievePointsText()
                        } else {
                            retrieveAuthStatusText()?.let {
                                root.context.getString(it)
                            }
                        }
                    loyaltyValueExtra.text = retrieveAuthSuffix()


                    if (!shouldShowLinkStatus()) {
                        linkStatusText.visibility = View.GONE
                    } else {
                        retrieveLinkStatusText()?.let {
                            linkStatusText.visibility = View.VISIBLE
                            linkStatusText.text = root.context.getString(it)
                        }
                    }
                    if (shouldShowLinkImages()) {
                        linkStatusImg.visibility = View.VISIBLE
                        retrieveLinkImage()?.let {
                            linkStatusImg.setImageDrawable(
                                root.context.getDrawable(it)
                            )
                        }
                    } else {
                        linkStatusImg.visibility = View.GONE
                    }
                }
            }
        }

        private fun bindSecondaryColorChanges(binding: CardItemBinding, primaryColor: Int) {
            val textColor: Int
            if (ColorUtil.isColorLight(
                    primaryColor,
                    ColorUtil.LIGHT_THRESHOLD_TEXT
                )
            ) {
                textColor = android.R.color.black
            } else {
                textColor = android.R.color.white
            }

            binding.companyName.setTextColor(binding.root.context.getColor(textColor))
            binding.loyaltyValue.setTextColor(binding.root.context.getColor(textColor))
            binding.loyaltyValueExtra.setTextColor(binding.root.context.getColor(textColor))
            binding.linkStatusText.setTextColor(binding.root.context.getColor(textColor))
            binding.linkStatusImg.setColorFilter(
                ContextCompat.getColor(
                    binding.root.context,
                    textColor
                ), android.graphics.PorterDuff.Mode.MULTIPLY
            )
        }
    }

    inner class PlanSuggestionHolder(val binding: EmptyLoyaltyItemBinding) :
        BaseViewHolder<MembershipPlan>(binding) {

        override fun bind(item: MembershipPlan) {
            with(binding) {
                membershipPlan = item
                dismissBanner.setOnClickListener {
                    onRemoveListener(membershipCards[adapterPosition] as MembershipPlan)
                }
                joinCardMainLayout.setOnClickListener { onClickListener(item) }
            }
        }
    }
}