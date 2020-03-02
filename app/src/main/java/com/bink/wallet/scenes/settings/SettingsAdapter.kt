package com.bink.wallet.scenes.settings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleObserver
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.databinding.SettingsHeaderBinding
import com.bink.wallet.databinding.SettingsItemBinding
import com.bink.wallet.model.ListLiveData
import com.bink.wallet.model.SettingsItem
import com.bink.wallet.model.SettingsItemType
import com.bink.wallet.utils.setSafeOnClickListener

class SettingsAdapter(
    private val itemsList: ListLiveData<SettingsItem>,
    val itemClickListener: (SettingsItem) -> Unit = {}
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(), LifecycleObserver {

    companion object {
        const val HEADER = 0
        const val ITEM = 1
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == HEADER) {
            SettingsHeaderViewHolder(SettingsHeaderBinding.inflate(inflater))
        } else {
            SettingsViewHolder(SettingsItemBinding.inflate(inflater), itemClickListener)
        }
    }

    override fun getItemCount(): Int = itemsList.size

    override fun getItemViewType(position: Int): Int {
        return if (itemsList[position]?.type == SettingsItemType.HEADER) {
            HEADER
        } else {
            ITEM
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        itemsList[position]?.let {
            if (it.type == SettingsItemType.HEADER) {
                (holder as SettingsHeaderViewHolder).bind(it)
            } else {
                var separator = false
                if (position < itemsList.size - 1) {
                    if (itemsList[position + 1]?.type != SettingsItemType.HEADER) {
                        separator = true
                    }
                }
                (holder as SettingsViewHolder).bind(it, separator)
            }
        }
    }

    class SettingsViewHolder(
        val binding: SettingsItemBinding,
        val itemClickListener: (SettingsItem) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SettingsItem, separator: Boolean) {
            with(binding) {
                this.item = item
                value.visibility =
                    if (item.value.isNullOrEmpty()) {
                        View.GONE
                    } else {
                        View.VISIBLE
                    }
                environmentSpacer.visibility =
                    if (separator) {
                        View.VISIBLE
                    } else {
                        View.INVISIBLE
                    }
                root.setSafeOnClickListener { itemClickListener(item) }
                executePendingBindings()
            }
        }
    }

    class SettingsHeaderViewHolder(
        val binding: SettingsHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SettingsItem) {
            binding.item = item
            binding.executePendingBindings()
        }
    }
}