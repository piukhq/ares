package com.bink.wallet.scenes.loyalty_wallet.wallet.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.databinding.CardOnboardingItemBinding
import com.bink.wallet.databinding.CardOnboardingSeeStoreBinding
import com.bink.wallet.databinding.LoyaltyWalletItemBinding
import com.bink.wallet.model.BannerDisplay
import com.bink.wallet.model.JoinCardItem
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.model.response.payment_card.PaymentCard
import com.bink.wallet.scenes.BaseViewHolder
import com.bink.wallet.scenes.loyalty_wallet.wallet.adapter.viewholders.CardOnBoardingLinkViewHolder
import com.bink.wallet.scenes.loyalty_wallet.wallet.adapter.viewholders.CardOnBoardingSeeViewHolder
import com.bink.wallet.scenes.loyalty_wallet.wallet.adapter.viewholders.CardOnBoardingStoreViewHolder
import com.bink.wallet.scenes.loyalty_wallet.wallet.adapter.viewholders.LoyaltyWalletViewHolder
import com.bink.wallet.utils.WalletOrderingUtil
import com.bink.wallet.utils.local_point_scraping.WebScrapableManager
import java.util.*
import kotlin.collections.ArrayList
import kotlin.properties.Delegates

class LoyaltyWalletAdapter(
    val onClickListener: (Any) -> Unit = {},
    val onCardLinkClickListener: (MembershipPlan) -> Unit = {},
    var onPlaceholderClickListener: (Any) -> Unit = {}
) : RecyclerView.Adapter<BaseViewHolder<*>>() {

    companion object {
        private const val MEMBERSHIP_CARD = 0

        private const val CARD_ON_BOARDING_PLL = 1

        private const val CARD_ON_BOARDING_SEE = 2

        private const val CARD_ON_BOARDING_STORE = 3
    }

    var membershipCards: ArrayList<Any> by Delegates.observable(ArrayList()) { _, oldList, newList ->
        notifyChanges(oldList, newList)
    }

    var membershipPlans = ArrayList<MembershipPlan>()

    var paymentCards: MutableList<PaymentCard>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {

            MEMBERSHIP_CARD -> LoyaltyWalletViewHolder(
                LoyaltyWalletItemBinding.inflate(inflater),
                onClickListener,
                membershipPlans,
                paymentCards
            )

            CARD_ON_BOARDING_SEE -> CardOnBoardingSeeViewHolder(
                CardOnboardingSeeStoreBinding.inflate(inflater),
                onClickListener,
                onCardLinkClickListener,
                membershipPlans
            )

            CARD_ON_BOARDING_STORE -> CardOnBoardingStoreViewHolder(
                CardOnboardingSeeStoreBinding.inflate(inflater),
                onClickListener,
                onCardLinkClickListener,
                onPlaceholderClickListener,
                membershipPlans
            )

            else -> CardOnBoardingLinkViewHolder(
                CardOnboardingItemBinding.inflate(inflater),
                onClickListener,
                onCardLinkClickListener,
                membershipPlans
            )

        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        membershipCards[position].let {
            when (holder) {
                is LoyaltyWalletViewHolder -> holder.bind(it as MembershipCard)
                is CardOnBoardingLinkViewHolder -> holder.bind(it as MembershipPlan)
                is CardOnBoardingSeeViewHolder -> holder.bind(it as MembershipPlan)
                is CardOnBoardingStoreViewHolder -> holder.bind(it as MembershipPlan)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            membershipCards[position] is MembershipCard -> MEMBERSHIP_CARD
            (membershipCards[position] as MembershipPlan).isStoreCard() -> CARD_ON_BOARDING_STORE
            WebScrapableManager.isCardScrapable(
                (membershipCards[position] as MembershipPlan).id
            ) -> CARD_ON_BOARDING_SEE
            else -> CARD_ON_BOARDING_PLL
        }
    }

    override fun getItemCount(): Int = membershipCards.size

    override fun getItemId(position: Int): Long =
        when (val walletItem = membershipCards[position]) {
            is MembershipCard -> walletItem.id.toLong()
            is MembershipPlan -> walletItem.id.toLong()
            else -> position.toLong()
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

    fun onItemMove(fromPosition: Int?, toPosition: Int?): Boolean {
        fromPosition?.let {
            toPosition?.let {
                if (getItemViewType(toPosition) != MEMBERSHIP_CARD) {
                    notifyItemMoved(fromPosition, toPosition)
                    return false
                }

                if (fromPosition < toPosition) {
                    for (i in fromPosition until toPosition) {
                        Collections.swap(membershipCards, i, i + 1)
                    }
                } else {
                    for (i in fromPosition downTo toPosition + 1) {
                        Collections.swap(membershipCards, i, i - 1)
                    }
                }
                notifyItemMoved(fromPosition, toPosition)
                WalletOrderingUtil.setSavedLoyaltyCardWallet(membershipCards)
                return true
            }
        }
        return false
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

}