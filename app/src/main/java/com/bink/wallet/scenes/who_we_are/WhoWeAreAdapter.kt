package com.bink.wallet.scenes.who_we_are

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.R
import com.bink.wallet.databinding.WhoWeAreItemBinding

class WhoWeAreAdapter(
    private val names: Array<String>
) :
    RecyclerView.Adapter<WhoWeAreAdapter.WhoWeAreViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WhoWeAreViewHolder =
        WhoWeAreViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.who_we_are_item,
                parent,
                false
            )
        )


    override fun onBindViewHolder(holder: WhoWeAreViewHolder, position: Int) {
        holder.bind(names[position])
    }

    override fun getItemCount(): Int {
        return names.size
    }

    class WhoWeAreViewHolder(val binding: WhoWeAreItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(name: String) {
            binding.name.text = name
        }
    }
}