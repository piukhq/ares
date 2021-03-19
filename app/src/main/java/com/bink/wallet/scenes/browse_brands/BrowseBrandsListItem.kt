package com.bink.wallet.scenes.browse_brands

import androidx.annotation.StringRes
import com.bink.wallet.R
import com.bink.wallet.model.response.membership_plan.MembershipPlan

sealed class BrowseBrandsListItem(val id: Int) {
    data class BrandItem(
        val membershipPlan: MembershipPlan,
        val isPlanInLoyaltyWallet: Boolean = false,
        var hasSeparator: Boolean
    ) : BrowseBrandsListItem(R.layout.item_brand)

    data class SectionTitleItem(@StringRes val sectionTitle: Int,@StringRes val sectionDescription: Int) :
        BrowseBrandsListItem(R.layout.item_brands_section_title)
}