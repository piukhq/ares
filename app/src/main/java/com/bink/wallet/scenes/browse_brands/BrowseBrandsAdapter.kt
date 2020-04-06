package com.bink.wallet.scenes.browse_brands

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.BrandItemBinding
import com.bink.wallet.BrandsSectionTitleBinding
import com.bink.wallet.R
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.utils.enums.CardType


class BrowseBrandsAdapter(
    private val brands: List<BrowseBrandsListItem>,
    private val splitPosition: Int,
    val itemClickListener: (MembershipPlan) -> Unit = {}
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            BRAND_ITEM -> {
                BrandsViewHolder(
                    DataBindingUtil.inflate<BrandItemBinding>(
                        LayoutInflater.from(parent.context),
                        R.layout.item_brand,
                        parent,
                        false
                    ).apply {
                        container.setOnClickListener {
                            item?.apply {
                                itemClickListener(this)
                            }
                        }
                    })
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
            BRAND_ITEM -> (brands[position] as BrowseBrandsListItem.MembershipPlanItem).let {
                (holder as BrandsViewHolder).bind(
                    it.membershipPlan,
                    position == itemCount - 1 || position == splitPosition
                )
            }
            SECTION_TITLE_ITEM -> {
                (holder as SectionTitleViewHolder).bind(
                    (brands[position] as BrowseBrandsListItem.BrandsSectionTitleItem).sectionTitle
                )
            }
        }
    }

    class SectionTitleViewHolder(val binding: BrandsSectionTitleBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(sectionTitle: String) {
            binding.sectionTitle.text = sectionTitle
        }
    }

    class BrandsViewHolder(val binding: BrandItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MembershipPlan, isLast: Boolean) {
            binding.item = item
            binding.browseBrandsDescription.visibility =
                if (item.getCardType() == CardType.PLL) {
                    View.VISIBLE
                } else {
                    View.GONE
                }

            binding.separator.visibility = if (isLast) {
                View.GONE
            } else {
                View.VISIBLE
            }
        }
    }

    companion object {
        private const val BRAND_ITEM = R.layout.item_brand
        private const val SECTION_TITLE_ITEM = R.layout.item_brands_section_title
    }
}