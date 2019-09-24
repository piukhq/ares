package com.bink.wallet.scenes.settings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.databinding.SettingsItemBinding
import com.bink.wallet.model.SettingsItem
import com.bink.wallet.model.SettingsItemType

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
        binding.apply {
            root.setOnClickListener {
                item?.apply {
                    itemClickListener(this)
                }
            }
        }
        return SettingsViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: SettingsViewHolder, position: Int) {
        items[position].let { holder.bind(it) }
    }

    fun setItems(newItems: ArrayList<SettingsItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
    fun setEmail(newEmail: String) {
        val item = items[2]
        val newItem = SettingsItem(item.title, newEmail, item.type)
        items[2] = newItem
        notifyItemChanged(2)
    }

    class SettingsViewHolder(val binding: SettingsItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SettingsItem) {
            binding.title.text = item.title
            binding.value.text = item.value
            binding.rightArrow.visibility =
                if (item.type == SettingsItemType.EMAIL_ADDRESS)
                    View.VISIBLE
                else
                    View.GONE
            binding.executePendingBindings()
        }
    }

}