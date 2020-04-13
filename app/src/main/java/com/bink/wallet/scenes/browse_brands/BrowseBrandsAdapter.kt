package com.bink.wallet.scenes.browse_brands

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.R
import com.bink.wallet.model.response.membership_plan.MembershipPlan

typealias OnBrandItemClickListener = (MembershipPlan) -> Unit

class BrowseBrandsAdapter(
    private var brands: List<BrowseBrandsListItem> = listOf()
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
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

    override fun getItemCount() = brands.size

    override fun getItemViewType(position: Int): Int = brands[position].id

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            BRAND_ITEM -> (brands[position] as BrowseBrandsListItem.BrandItem).let {
                (holder as BrandsViewHolder).bind(
                    it,
                    position == itemCount - 1 ||
                            brands[position + 1] is BrowseBrandsListItem.SectionTitleItem,
                    onBrandItemClickListener
                )
            }
            SECTION_TITLE_ITEM -> {
                (holder as SectionTitleViewHolder).bind(
                    (brands[position] as BrowseBrandsListItem.SectionTitleItem).sectionTitle
                )
            }
        }
    }

    fun setOnBrandItemClickListener(onBrandItemClickListener: OnBrandItemClickListener?) {
        this.onBrandItemClickListener = onBrandItemClickListener
    }

    fun setSearchResultList(newBrandsList: List<BrowseBrandsListItem>) {
        val oldBrands = this.brands
        this.brands = newBrandsList
        DiffUtil.calculateDiff(
            BrandItemsDiffUtil(newBrandsList, oldBrands), true
        ).dispatchUpdatesTo(this)
    }

    companion object {
        private const val BRAND_ITEM = R.layout.item_brand
        private const val SECTION_TITLE_ITEM = R.layout.item_brands_section_title
    }
}