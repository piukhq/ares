package com.bink.wallet.scenes.browse_brands

import android.annotation.SuppressLint
import androidx.recyclerview.widget.DiffUtil

object BrandItemsDiffUtil :
    DiffUtil.ItemCallback<BrowseBrandsListItem>() {
    override fun areItemsTheSame(
        oldItem: BrowseBrandsListItem,
        newItem: BrowseBrandsListItem
    ): Boolean =
        if (oldItem is BrowseBrandsListItem.BrandItem && newItem is BrowseBrandsListItem.BrandItem) {
            oldItem.membershipPlan.id == newItem.membershipPlan.id
        } else {
            oldItem.id == newItem.id
        }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(
        oldItem: BrowseBrandsListItem,
        newItem: BrowseBrandsListItem
    ): Boolean =
        if (oldItem is BrowseBrandsListItem.BrandItem && newItem is BrowseBrandsListItem.BrandItem) {
            oldItem.membershipPlan === newItem.membershipPlan &&
                    oldItem.hasSeparator == newItem.hasSeparator
        } else if (oldItem is BrowseBrandsListItem.SectionTitleItem &&
            newItem is BrowseBrandsListItem.SectionTitleItem
        ) {
            oldItem.sectionTitle == newItem.sectionTitle
        } else {
            false
        }

}