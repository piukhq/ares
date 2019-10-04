package com.bink.wallet.scenes.settings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleObserver
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.databinding.SettingsItemBinding
import com.bink.wallet.model.ListHolder
import com.bink.wallet.model.ListLiveData
import com.bink.wallet.model.SettingsItem
import com.bink.wallet.model.SettingsItemType

class SettingsAdapter(
    private val itemsList: ListLiveData<SettingsItem>,
    val itemClickListener: (SettingsItem) -> Unit = {}
) :
    RecyclerView.Adapter<SettingsAdapter.SettingsViewHolder>(), LifecycleObserver {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SettingsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = SettingsItemBinding.inflate(inflater)
        return SettingsViewHolder(binding, itemClickListener)
    }

    override fun getItemCount(): Int = itemsList.size

    override fun onBindViewHolder(holder: SettingsViewHolder, position: Int) {
        itemsList[position].let { holder.bind(it!!) }
    }

    class SettingsViewHolder(val binding: SettingsItemBinding,
                             val itemClickListener: (SettingsItem) -> Unit) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SettingsItem) {
            binding.item = item
            binding.rightArrow.visibility =
                if (item.type == SettingsItemType.EMAIL_ADDRESS)
                    View.VISIBLE
                else
                    View.GONE
            binding.root.setOnClickListener { itemClickListener(item) }
            binding.executePendingBindings()
        }
    }
}