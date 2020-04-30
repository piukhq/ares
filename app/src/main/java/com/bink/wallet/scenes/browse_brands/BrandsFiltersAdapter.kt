package com.bink.wallet.scenes.browse_brands

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.ItemFilterBinding
import com.bink.wallet.R

typealias OnFilterClickListener = (BrandsFilter) -> Unit

class BrandsFiltersAdapter : RecyclerView.Adapter<BrandsFiltersAdapter.FiltersViewHolder>() {

    private var filters = listOf<BrandsFilter>()
    private var onFilterClickListener: OnFilterClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FiltersViewHolder =
        FiltersViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_filter,
                parent,
                false
            )
        )

    override fun getItemCount(): Int = filters.size

    override fun onBindViewHolder(holder: FiltersViewHolder, position: Int) {
        holder.bind(
            filters[position],
            isLastRow(position),
            onFilterClickListener
        )
    }

    private fun isLastRow(position: Int) = if (filters.size % 2 == 0) {
        position == filters.size - 1 || position == filters.size - 2
    } else {
        position == filters.size - 1
    }


    fun setFilters(filters: List<BrandsFilter>) {
        this.filters = filters
        this.notifyDataSetChanged()
    }

    fun setOnFilterClickListener(onFilterClickListener: OnFilterClickListener) {
        this.onFilterClickListener = onFilterClickListener
    }

    class FiltersViewHolder(val binding: ItemFilterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            filter: BrandsFilter,
            isLastRow: Boolean,
            onFilterClickListener: OnFilterClickListener?
        ) {
            binding.filter = filter

            if (isLastRow) {
                binding.separator.visibility = View.GONE
            }

            binding.checkbox.setOnClickListener {
                filter.isChecked = binding.checkbox.isChecked
                onFilterClickListener?.invoke(filter)
            }
        }
    }

}