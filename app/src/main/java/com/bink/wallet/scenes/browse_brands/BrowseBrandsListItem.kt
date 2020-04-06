package com.bink.wallet.scenes.browse_brands

import com.bink.wallet.R
import com.bink.wallet.model.response.membership_plan.MembershipPlan

sealed class BrowseBrandsListItem(val id: Int) {
    data class MembershipPlanItem(
        val membershipPlan: MembershipPlan,
        val isPlanInLoyaltyWallet: Boolean = false
    ) : BrowseBrandsListItem(R.layout.item_brand)

    data class BrandsSectionTitleItem(val sectionTitle: String) :
        BrowseBrandsListItem(R.layout.item_pll_title)
}