package com.bink.wallet.scenes.browse_brands

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.R
import com.bink.wallet.model.response.membership_plan.MembershipPlan

typealias OnBrandItemClickListener = (MembershipPlan) -> Unit

class BrowseBrandsAdapter :
    ListAdapter<BrowseBrandsListItem, RecyclerView.ViewHolder>(BrandItemsDiffUtil) {

    private var onBrandItemClickListener: OnBrandItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            BRAND_ITEM -> {
                BrandsViewHolder(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.item_brand,
                        parent,
                        false
                    )
                )
            }
            else -> {
                SectionTitleViewHolder(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(
                            parent.context
                        ),
                        R.layout.item_brands_section_title,
                        parent,
                        false
                    )
                )
            }
        }

    override fun getItemViewType(position: Int): Int = getItem(position).id

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            BRAND_ITEM -> (getItem(position) as BrowseBrandsListItem.BrandItem).let {
                (holder as BrandsViewHolder).bind(
                    it,
                    onBrandItemClickListener
                )
            }
            SECTION_TITLE_ITEM -> {
                val item = (getItem(position) as BrowseBrandsListItem.SectionTitleItem)
                (holder as SectionTitleViewHolder).bind(
                    item.sectionTitle, item.sectionDescription
                )
            }
        }
    }

    fun setOnBrandItemClickListener(onBrandItemClickListener: OnBrandItemClickListener?) {
        this.onBrandItemClickListener = onBrandItemClickListener
    }

    companion object {
        private const val BRAND_ITEM = R.layout.item_brand
        private const val SECTION_TITLE_ITEM = R.layout.item_brands_section_title
    }
}