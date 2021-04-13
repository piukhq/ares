package com.bink.wallet.scenes.loyalty_wallet.wallet.adapter.viewholders

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.R
import com.bink.wallet.databinding.CardOnboardingSeeStoreBinding
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.scenes.BaseViewHolder
import com.bink.wallet.scenes.loyalty_wallet.onboarding.CardOnBoardingSeeStoreAdapter

class CardOnBoardingStoreViewHolder(
    val binding: CardOnboardingSeeStoreBinding,
    val onClickListener: (Any) -> Unit = {},
    onCardLinkClickListener: (MembershipPlan) -> Unit = {},
    onPlaceholderClickListener: (Any) -> Unit = {},
    membershipPlans: ArrayList<MembershipPlan>
) :
    BaseViewHolder<MembershipPlan>(binding) {

    private val seeStoreRecyclerView: RecyclerView = binding.rvSeeStoreItems
    private val seeStoreAdapter = CardOnBoardingSeeStoreAdapter(onCardLinkClickListener, onPlaceholderClickListener)

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