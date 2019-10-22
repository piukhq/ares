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
        val item = itemsList[position]
        return if (item!!.type == SettingsItemType.HEADER) {
            HEADER
        } else {
            ITEM
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        itemsList[position].let {
            if (it!!.type == SettingsItemType.HEADER) {
                (holder as SettingsHeaderViewHolder).bind(it)
            } else {
                var separator: Boolean = false
                if (position < itemsList.size - 1) {
                    val next = itemsList[position + 1]
                    if (next!!.type != SettingsItemType.HEADER) {
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
            binding.item = item
            binding.value.visibility =
                if (item.value.isNullOrEmpty()) {
                    View.GONE
                } else {
                    View.VISIBLE
                }
            binding.rightArrow.visibility =
                if (listOf(SettingsItemType.VERSION_NUMBER, SettingsItemType.BASE_URL)
                        .contains(item.type)
                ) {
                    View.GONE
                } else {
                    View.VISIBLE
                }
            binding.environmentSpacer.visibility =
                if (separator) {
                    View.VISIBLE
                } else {
                    View.INVISIBLE
                }
            binding.root.setOnClickListener { itemClickListener(item) }
            binding.executePendingBindings()
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