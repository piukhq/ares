package com.bink.wallet.model

import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView

class ListLiveData<T> : MutableLiveData<ListHolder<T>>() {
    val size: Int
        get() = value?.size() ?: -1

    fun addItem(item: T, position: Int = value?.size() ?: 0) {
        value?.addItem(position, item)
        value = value
    }

    operator fun get(position: Int): T? {
        return value?.list?.get(position)
    }
}

data class ListHolder<T>(val list: MutableList<T> = mutableListOf()) {
    var indexChanged: Int = -1
    private var updateType: UpdateType? = null


    fun addItem(position: Int, item: T) {
        list.add(position, item)
        indexChanged = position
        updateType = UpdateType.INSERT
    }

    fun size(): Int {
        return list.size
    }

    private enum class UpdateType {
        INSERT {
            override fun notifyChange(
                adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>,
                indexChanged: Int
            ) = adapter.notifyItemInserted(indexChanged)
        };

        abstract fun notifyChange(
            adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>,
            indexChanged: Int
        )
    }
}