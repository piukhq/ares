package com.bink.wallet.scenes.loyalty_wallet

import android.graphics.Canvas
import android.util.Log
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.scenes.payment_card_wallet.PaymentCardWalletAdapter
import kotlinx.android.synthetic.main.barcode_fragment.view.*

class RecyclerItemTouchHelper(
    dragDirs: Int,
    swipeDirs: Int,
    private val listener: RecyclerItemTouchHelperListener
) :
    ItemTouchHelper.SimpleCallback(dragDirs, swipeDirs) {

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return true
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        if (viewHolder != null) {
            val foregroundView = when (viewHolder) {
                is LoyaltyWalletAdapter.LoyaltyWalletViewHolder ->
                    viewHolder.binding.cardItem.mainLayout
                is PaymentCardWalletAdapter.PaymentCardWalletHolder ->
                    viewHolder.binding.mainPayment
                else ->
                    null
            }
            if (foregroundView != null) {
                getDefaultUIUtil().onSelected(foregroundView)
            }
        }
    }

    override fun onChildDrawOver(
        c: Canvas, recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float,
        actionState: Int, isCurrentlyActive: Boolean
    ) {
        val foregroundView = when (viewHolder) {
            is LoyaltyWalletAdapter.LoyaltyWalletViewHolder ->
                viewHolder.binding.cardItem.mainLayout
            is PaymentCardWalletAdapter.PaymentCardWalletHolder ->
                viewHolder.binding.mainPayment
            else ->
                null
        }
        if (foregroundView != null) {
            getDefaultUIUtil().onDrawOver(
                c, recyclerView, foregroundView, dX, dY,
                actionState, isCurrentlyActive
            )
        }
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        val foregroundView = when (viewHolder) {
            is LoyaltyWalletAdapter.LoyaltyWalletViewHolder ->
                viewHolder.binding.cardItem.mainLayout
            is PaymentCardWalletAdapter.PaymentCardWalletHolder ->
                viewHolder.binding.mainPayment
            else ->
                null
        }
        if (foregroundView != null) {
            getDefaultUIUtil().clearView(foregroundView)
        }
    }

    override fun onChildDraw(
        c: Canvas, recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float,
        actionState: Int, isCurrentlyActive: Boolean
    ) {
        val foregroundView = when (viewHolder) {
            is LoyaltyWalletAdapter.LoyaltyWalletViewHolder ->
                viewHolder.binding.cardItem.mainLayout
            is PaymentCardWalletAdapter.PaymentCardWalletHolder ->
                viewHolder.binding.mainPayment
            else ->
                null
        }

        if (foregroundView != null) {
            getDefaultUIUtil().onDraw(c, recyclerView, foregroundView, dX, dY, actionState, isCurrentlyActive)
        }
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        listener.onSwiped(viewHolder, direction, viewHolder.adapterPosition)
    }

    interface RecyclerItemTouchHelperListener {
        fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int, position: Int)
    }
}