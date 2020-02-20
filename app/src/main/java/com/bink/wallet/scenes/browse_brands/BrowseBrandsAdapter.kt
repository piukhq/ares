package com.bink.wallet.scenes.browse_brands

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.databinding.BrandListItemBinding
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.utils.enums.CardType
import com.bink.wallet.utils.toPixelFromDip


class BrowseBrandsAdapter(
    private val brands: List<Pair<String?, MembershipPlan>>,
    private val splitPosition: Int,
    val itemClickListener: (MembershipPlan) -> Unit = {}
) : RecyclerView.Adapter<BrowseBrandsAdapter.BrandsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BrandsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = BrandListItemBinding.inflate(inflater)
        binding.apply {
            root.setOnClickListener {
                item?.apply {
                    itemClickListener(this)
                }
            }
        }
        return BrandsViewHolder(binding)
    }

    override fun getItemCount() = brands.size

    override fun onBindViewHolder(holder: BrandsViewHolder, position: Int) {
        brands[position].let {
            holder.bind(
                it,
                position == itemCount - 1 || position == splitPosition
            )
        }
    }

    class BrandsViewHolder(val binding: BrandListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val titleMarginTop = 24f

        fun bind(item: Pair<String?, MembershipPlan>, isLast: Boolean) {
            if (item.first != null) {
                binding.sectionTitle.text = item.first
                binding.sectionTitle.visibility = View.VISIBLE
            } else {
                binding.sectionTitle.visibility = View.GONE
            }

            if (item.second.getCardType() == CardType.PLL) {
                resetTitlePosition()
            } else {
                centerPlanTitlePosition()
            }

            binding.separator.visibility = if (isLast) {
                View.GONE
            } else {
                View.VISIBLE
            }

            binding.item = item.second
            binding.executePendingBindings()
        }

        private fun centerPlanTitlePosition() {
            binding.browseBrandsDescription.visibility = View.GONE
            val constraintSet = ConstraintSet()
            constraintSet.apply {
                clone(binding.constraintLayout)
                connect(
                    binding.browseBrandsTitle.id,
                    ConstraintSet.BOTTOM,
                    binding.constraintLayout.id,
                    ConstraintSet.BOTTOM,
                    0
                )
                connect(
                    binding.browseBrandsTitle.id,
                    ConstraintSet.TOP,
                    binding.constraintLayout.id,
                    ConstraintSet.TOP,
                    0
                )
                applyTo(binding.constraintLayout)
            }
        }

        private fun resetTitlePosition() {
            binding.browseBrandsDescription.visibility = View.VISIBLE
            val constraintSet = ConstraintSet()
            constraintSet.apply {
                clone(binding.constraintLayout)
                connect(
                    binding.browseBrandsTitle.id,
                    ConstraintSet.BOTTOM,
                    -1,
                    ConstraintSet.BOTTOM,
                    0
                )
                connect(
                    binding.browseBrandsTitle.id,
                    ConstraintSet.TOP,
                    binding.constraintLayout.id,
                    ConstraintSet.TOP,
                    binding.root.context.toPixelFromDip(titleMarginTop).toInt()
                )
                applyTo(binding.constraintLayout)
            }
        }
    }
}