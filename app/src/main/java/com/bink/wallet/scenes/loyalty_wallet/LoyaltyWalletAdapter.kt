package com.bink.wallet.scenes.loyalty_wallet

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.R
import com.bink.wallet.data.SharedPreferenceManager
import com.bink.wallet.databinding.CardItemBinding
import com.bink.wallet.databinding.CardOnboardingItemBinding
import com.bink.wallet.databinding.CardOnboardingSeeStoreBinding
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
import com.bink.wallet.utils.WalletOrderingUtil
import com.bink.wallet.utils.bindings.setVoucherCollectedProgress
import com.bink.wallet.utils.displayVoucherEarnAndTarget
import com.bink.wallet.utils.enums.MembershipCardStatus
import com.bink.wallet.utils.enums.VoucherStates
import java.util.*
import kotlin.collections.ArrayList
import com.bink.wallet.utils.formatBalance
import com.bink.wallet.utils.local_point_scraping.WebScrapableManager
import kotlin.properties.Delegates

class LoyaltyWalletAdapter(
    val onClickListener: (Any) -> Unit = {},
    val onCardLinkClickListener: (MembershipPlan) -> Unit = {}
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
            MEMBERSHIP_CARD -> LoyaltyWalletViewHolder(LoyaltyWalletItemBinding.inflate(inflater))
            CARD_ON_BOARDING_SEE -> CardOnBoardingSeeViewHolder(
                CardOnboardingSeeStoreBinding.inflate(inflater)
            )
            CARD_ON_BOARDING_STORE -> CardOnBoardingStoreViewHolder(CardOnboardingSeeStoreBinding.inflate(inflater))
            else -> CardOnBoardingLinkViewHolder(CardOnboardingItemBinding.inflate(inflater))

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
                (membershipCards[position] as MembershipPlan).id) -> CARD_ON_BOARDING_SEE
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

    fun onItemMove(fromPosition: Int?, toPosition: Int?): Boolean {
        fromPosition?.let {
            toPosition?.let {
                if (getItemViewType(toPosition) == CARD_ON_BOARDING_PLL) {
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

    inner class LoyaltyWalletViewHolder(val binding: LoyaltyWalletItemBinding) :
        BaseViewHolder<MembershipCard>(binding) {

        override fun bind(item: MembershipCard) {
            val cardBinding = binding.cardItem
            if (!membershipPlans.isNullOrEmpty()) {

                membershipPlans.firstOrNull { it.id == item.membership_plan }
                    ?.let { membershipPlan ->
                        paymentCards?.let {
                            val loyaltyItem = LoyaltyWalletItem(item, membershipPlan, it)
                            bindCardToLoyaltyItem(loyaltyItem, binding)
                        }

                        membershipPlan.card?.let { membershipPlanCard ->
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
                        item.balances?.firstOrNull()?.let { balance ->
                            when (balance.prefix != null) {
                                true ->
                                    loyaltyValue.text = balance.formatBalance()

                                else -> {
                                    loyaltyValue.text = balance.value
                                    loyaltyValueExtra.text = balance.suffix
                                }
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

    inner class CardOnBoardingLinkViewHolder(val binding: CardOnboardingItemBinding) :
        BaseViewHolder<MembershipPlan>(binding) {

        private val gridRecyclerView: RecyclerView = binding.rvImageGrid
        private val cardOnBoardLinkAdapter = CardOnboardLinkAdapter(onCardLinkClickListener)

        init {
            val context = binding.root.context
            gridRecyclerView.layoutManager = GridLayoutManager(context, 2)
            val state =
                if (SharedPreferenceManager.cardOnBoardingState > 0) SharedPreferenceManager.cardOnBoardingState else 4
            cardOnBoardLinkAdapter.setPlansData(membershipPlans.filter { it.isPlanPLL() }
                .sortedByDescending { it.id }.take(state))
            gridRecyclerView.adapter = cardOnBoardLinkAdapter
        }

        override fun bind(item: MembershipPlan) {
            with(binding) {
                mainContainer.setOnClickListener {
                    onClickListener(item)
                }
            }
        }

    }

    inner class CardOnBoardingSeeViewHolder(val binding: CardOnboardingSeeStoreBinding) :
        BaseViewHolder<MembershipPlan>(binding) {

        private val seeStoreRecyclerView: RecyclerView = binding.rvSeeStoreItems
        private val seeStoreAdapter = CardOnBoardingSeeStoreAdapter(onCardLinkClickListener)

        init {
            val context = binding.root.context
            seeStoreRecyclerView.layoutManager = GridLayoutManager(context, 5)
            seeStoreAdapter.setPlansData(membershipPlans.filter {WebScrapableManager.isCardScrapable(it.id) }.sortedByDescending { it.id })
            seeStoreRecyclerView.adapter = seeStoreAdapter
        }

        override fun bind(item: MembershipPlan) {
            with(binding) {
                    tvSeeStoreTitle.text = root.context.getString(R.string.see_points_balance_title)
                    tvSeeStoreDescription.text = root.context.getString(R.string.see_points_balance_description)
            }
            binding.root.setOnClickListener {
                onClickListener(item)
            }
        }
    }

    inner class CardOnBoardingStoreViewHolder(val binding: CardOnboardingSeeStoreBinding) :
        BaseViewHolder<MembershipPlan>(binding) {

        private val seeStoreRecyclerView: RecyclerView = binding.rvSeeStoreItems
        private val seeStoreAdapter = CardOnBoardingSeeStoreAdapter(onCardLinkClickListener)

        init {
            val context = binding.root.context
            seeStoreRecyclerView.layoutManager = GridLayoutManager(context, 5)
            seeStoreAdapter.setPlansData(membershipPlans.filter { it.isStoreCard() })
            seeStoreRecyclerView.adapter = seeStoreAdapter
        }

        override fun bind(item: MembershipPlan) {
            with(binding) {
                    tvSeeStoreTitle.text = root.context.getString(R.string.store_barcodes_title)
                    tvSeeStoreDescription.text = root.context.getString(R.string.store_barcodes_description)
            }
            binding.root.setOnClickListener {
                onClickListener(item)
            }
        }
    }
}