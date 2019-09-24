package com.bink.wallet.scenes.settings

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.databinding.SettingsItemBinding
import com.bink.wallet.model.SettingsItem

class SettingsAdapter(
    val items: ArrayList<SettingsItem>,
    val itemClickListener: (SettingsItem) -> Unit = {}
) :
    RecyclerView.Adapter<SettingsAdapter.SettingsViewHolder>() {


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SettingsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = SettingsItemBinding.inflate(inflater)
        return SettingsViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: SettingsViewHolder, position: Int) {
        items[position].let { holder.bind(it) }
    }

    class SettingsViewHolder(val binding: SettingsItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SettingsItem) {
            binding.title.text = item.title
            binding.value.text = item.value
            binding.executePendingBindings()
        }
    }

}