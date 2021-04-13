package com.bink.wallet.scenes.loyalty_wallet.adapter.viewholders

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.R
import com.bink.wallet.databinding.CardOnboardingSeeStoreBinding
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.scenes.BaseViewHolder
import com.bink.wallet.scenes.loyalty_wallet.CardOnBoardingSeeStoreAdapter
import com.bink.wallet.utils.local_point_scraping.WebScrapableManager

class CardOnBoardingSeeViewHolder(
    val binding: CardOnboardingSeeStoreBinding,
    val onClickListener: (Any) -> Unit = {},
    onCardLinkClickListener: (MembershipPlan) -> Unit = {},
    membershipPlans: ArrayList<MembershipPlan>
) :
    BaseViewHolder<MembershipPlan>(binding) {

    private val seeStoreRecyclerView: RecyclerView = binding.rvSeeStoreItems
    private val seeStoreAdapter = CardOnBoardingSeeStoreAdapter(onCardLinkClickListener)

    init {
        val context = binding.root.context
        seeStoreRecyclerView.layoutManager = GridLayoutManager(context, 5)
        seeStoreAdapter.setPlansData(membershipPlans.filter { WebScrapableManager.isCardScrapable(it.id) }.sortedByDescending { it.id })
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