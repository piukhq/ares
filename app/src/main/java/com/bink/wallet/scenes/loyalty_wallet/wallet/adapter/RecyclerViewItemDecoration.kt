package com.bink.wallet.scenes.loyalty_wallet.wallet.adapter

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class RecyclerViewItemDecoration : RecyclerView.ItemDecoration() {

    companion object {
        private const val MARGIN = 20
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {

        outRect.set(
            MARGIN,
            MARGIN,
            MARGIN,
            MARGIN
        )

    }
}