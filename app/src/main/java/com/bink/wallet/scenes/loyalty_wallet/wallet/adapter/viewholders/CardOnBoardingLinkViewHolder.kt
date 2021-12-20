package com.bink.wallet.scenes.loyalty_wallet.wallet.adapter.viewholders

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.data.SharedPreferenceManager
import com.bink.wallet.databinding.CardOnboardingItemBinding
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.scenes.BaseViewHolder
import com.bink.wallet.scenes.loyalty_wallet.onboarding.CardOnboardLinkAdapter
import com.bink.wallet.utils.LINKING_SUPPORT_ADD
import com.bink.wallet.utils.LINKING_SUPPORT_ENROL

class CardOnBoardingLinkViewHolder(
    val binding: CardOnboardingItemBinding,
    val onClickListener: (Any) -> Unit = {},
    onCardLinkClickListener: (MembershipPlan) -> Unit = {},
    membershipPlans: ArrayList<MembershipPlan>
) :
    BaseViewHolder<MembershipPlan>(binding) {

    private val gridRecyclerView: RecyclerView = binding.rvImageGrid
    private val cardOnBoardLinkAdapter = CardOnboardLinkAdapter(onCardLinkClickListener)

    init {
        val context = binding.root.context
        val state = if (SharedPreferenceManager.cardOnBoardingState > 0) SharedPreferenceManager.cardOnBoardingState else 4

        gridRecyclerView.layoutManager = GridLayoutManager(context, 2)
        cardOnBoardLinkAdapter.setPlansData(membershipPlans.filter { it.isPlanPLL() && it.feature_set?.linking_support?.contains(
            LINKING_SUPPORT_ADD
        ) == true || it.feature_set?.linking_support?.contains(LINKING_SUPPORT_ENROL) == true  }.sortedByDescending { it.id }.take(state))
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