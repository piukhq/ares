package com.bink.wallet.scenes.browse_brands

import android.view.LayoutInflater
import android.view.View
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
    private var onScanCardItemClickListener: View.OnClickListener? = null

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
            SCAN_CARD_ITEM -> {
                ScanCardViewHolder(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.item_scan_card,
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
            SCAN_CARD_ITEM -> {
                (holder as ScanCardViewHolder).bind(onScanCardItemClickListener)
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

    fun setOnScanItemClickListener(onScanCardItemClickListener: View.OnClickListener?) {
        this.onScanCardItemClickListener = onScanCardItemClickListener
    }

    companion object {
        private const val BRAND_ITEM = R.layout.item_brand
        private const val SECTION_TITLE_ITEM = R.layout.item_brands_section_title
        private const val SCAN_CARD_ITEM = R.layout.item_scan_card
    }
}