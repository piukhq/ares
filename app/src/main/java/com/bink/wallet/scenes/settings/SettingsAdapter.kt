package com.bink.wallet.scenes.settings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleObserver
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.data.SharedPreferenceManager
import com.bink.wallet.databinding.SettingsFooterBinding
import com.bink.wallet.databinding.SettingsHeaderBinding
import com.bink.wallet.databinding.SettingsItemBinding
import com.bink.wallet.model.ListLiveData
import com.bink.wallet.model.SettingsItem
import com.bink.wallet.model.SettingsItemType
import com.bink.wallet.utils.setSafeOnClickListener

class SettingsAdapter(
    private val itemsList: ListLiveData<SettingsItem>,
    val itemClickListener: (SettingsItem) -> Unit = {},
    val usersEmail: String
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(), LifecycleObserver {

    companion object {
        const val HEADER = 0
        const val ITEM = 1
        const val FOOTER = 2
        const val CONTACT_US = "Contact us"
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == HEADER) {
            SettingsHeaderViewHolder(SettingsHeaderBinding.inflate(inflater))
        } else if (viewType == FOOTER) {
            SettingsFooterViewHolder(SettingsFooterBinding.inflate(inflater, parent, false))
        } else {
            SettingsViewHolder(SettingsItemBinding.inflate(inflater), itemClickListener)
        }
    }

    override fun getItemCount(): Int = itemsList.size

    override fun getItemViewType(position: Int): Int {
        return if (itemsList[position]?.type == SettingsItemType.HEADER) {
            HEADER
        } else if (itemsList[position]?.type == SettingsItemType.FOOTER) {
            FOOTER
        } else {
            ITEM
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        itemsList[position]?.let {
            if (it.type == SettingsItemType.HEADER) {
                (holder as SettingsHeaderViewHolder).bind(it)
            } else if (it.type == SettingsItemType.FOOTER) {
                (holder as SettingsFooterViewHolder).bind(usersEmail)
            } else {
                var separator = false
                if (position < itemsList.size - 1) {
                    val nextItemType = itemsList[position + 1]?.type
                    if (nextItemType != SettingsItemType.HEADER &&
                        nextItemType != SettingsItemType.FOOTER
                    ) {
                        separator = true
                    }
                }
                (holder as SettingsViewHolder).bind(it, separator)
            }
        }
    }

    inner class SettingsViewHolder(
        val binding: SettingsItemBinding,
        val itemClickListener: (SettingsItem) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SettingsItem, separator: Boolean) {
            with(binding) {
                this.item = item

                if (item.title.equals(
                        CONTACT_US,
                        true
                    ) && SharedPreferenceManager.isResponseAvailable && !SharedPreferenceManager.hasContactUsBeenClicked
                ) {
                    binding.notificationOval.visibility = View.VISIBLE
                } else {
                    binding.notificationOval.visibility = View.GONE
                }

                value.visibility =
                    if (item.value.isNullOrEmpty()) {
                        View.INVISIBLE
                    } else {
                        View.VISIBLE
                    }
                environmentSpacer.visibility =
                    if (separator) {
                        View.VISIBLE
                    } else {
                        View.INVISIBLE
                    }
                root.setSafeOnClickListener {
                    if (item.title.equals(CONTACT_US, true)) {
                        binding.notificationOval.visibility = View.GONE
                        notifyItemChanged(adapterPosition)
                        SharedPreferenceManager.hasContactUsBeenClicked = true

                    }
                    itemClickListener(item)
                }
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