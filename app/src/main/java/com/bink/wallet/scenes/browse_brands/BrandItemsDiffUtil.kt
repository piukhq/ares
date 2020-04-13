package com.bink.wallet.scenes.browse_brands

import androidx.recyclerview.widget.DiffUtil

class BrandItemsDiffUtil(
    private val newList: List<BrowseBrandsListItem>,
    private val oldList: List<BrowseBrandsListItem>
) : DiffUtil.Callback() {

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        oldList[oldItemPosition].id == newList[newItemPosition].id

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val newItem = newList[newItemPosition]
        val oldItem = oldList[oldItemPosition]
        return if (newItem is BrowseBrandsListItem.BrandItem && oldItem is BrowseBrandsListItem.BrandItem) {
            newItem.membershipPlan === oldItem.membershipPlan
        } else if (newItem is BrowseBrandsListItem.SectionTitleItem && oldItem is BrowseBrandsListItem.SectionTitleItem) {
            newItem.sectionTitle == oldItem.sectionTitle
        } else {
            false
        }
    }
}