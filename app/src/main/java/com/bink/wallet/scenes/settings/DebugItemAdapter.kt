package com.bink.wallet.scenes.settings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.databinding.ItemDebugBinding
import com.bink.wallet.model.DebugItem
import com.bink.wallet.model.DebugItemType
import com.bink.wallet.model.ListLiveData
import com.bink.wallet.utils.setSafeOnClickListener

class DebugItemAdapter(
    private val debugItems: ListLiveData<DebugItem>,
    val itemClickListener: (DebugItem) -> Unit = {}
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        DebugItemViewHolder(
            ItemDebugBinding.inflate(
                LayoutInflater.from(parent.context)
            ), itemClickListener
        )

    override fun getItemCount(): Int = debugItems.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        debugItems[position]?.let {
            (holder as DebugItemViewHolder).bind(
                it,
                //Temporary change to give better UI
//                position != debugItems.size - 1
            true
            )
        }
    }

    class DebugItemViewHolder(
        val binding: ItemDebugBinding,
        val itemClickListener: (DebugItem) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DebugItem, separator: Boolean) {
            with(binding) {
                debugItem = item
                rightArrow.visibility = if (item.type == DebugItemType.ENVIRONMENT) {
                    View.VISIBLE
                } else {
                    View.GONE
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
}