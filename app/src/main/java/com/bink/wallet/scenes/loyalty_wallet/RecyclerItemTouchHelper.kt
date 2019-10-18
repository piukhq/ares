package com.bink.wallet.scenes.loyalty_wallet

import android.graphics.Canvas
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.scenes.payment_card_wallet.PaymentCardWalletAdapter


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
        val foregroundView: CardView
        if (viewHolder != null) {
            when (viewHolder) {
                is LoyaltyWalletAdapter.LoyaltyWalletViewHolder -> {
                    foregroundView = viewHolder.binding.cardItem.mainLayout
                    getDefaultUIUtil().onSelected(foregroundView)
                }
                is PaymentCardWalletAdapter.PaymentCardWalletHolder -> {
                    foregroundView = viewHolder.binding.mainPayment
                    getDefaultUIUtil().onSelected(foregroundView)
                }
            }
        }
    }

    override fun onChildDrawOver(
        c: Canvas, recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float,
        actionState: Int, isCurrentlyActive: Boolean
    ) {

        val foregroundView: CardView
        when (viewHolder) {
            is LoyaltyWalletAdapter.LoyaltyWalletViewHolder -> {
                foregroundView = viewHolder.binding.cardItem.mainLayout
                getDefaultUIUtil().onDrawOver(
                    c, recyclerView, foregroundView, dX, dY,
                    actionState, isCurrentlyActive
                )
            }
            is PaymentCardWalletAdapter.PaymentCardWalletHolder -> {
                foregroundView = viewHolder.binding.mainPayment
                getDefaultUIUtil().onDrawOver(
                    c, recyclerView, foregroundView, dX, dY,
                    actionState, isCurrentlyActive
                )
            }
        }
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        val foregroundView: CardView
        when (viewHolder) {
            is LoyaltyWalletAdapter.LoyaltyWalletViewHolder -> {
                foregroundView = viewHolder.binding.cardItem.mainLayout
                getDefaultUIUtil().clearView(foregroundView)
            }
            is PaymentCardWalletAdapter.PaymentCardWalletHolder -> {
                foregroundView = viewHolder.binding.mainPayment
                getDefaultUIUtil().clearView(foregroundView)
            }
        }
    }

    override fun onChildDraw(
        c: Canvas, recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float,
        actionState: Int, isCurrentlyActive: Boolean
    ) {
        val foregroundView: CardView
        when (viewHolder) {
            is LoyaltyWalletAdapter.LoyaltyWalletViewHolder -> {
                foregroundView = viewHolder.binding.cardItem.mainLayout
                getDefaultUIUtil().onDraw(
                    c, recyclerView, foregroundView, dX / 2, dY / 2,
                    actionState, isCurrentlyActive
                )
            }
            is PaymentCardWalletAdapter.PaymentCardWalletHolder -> {
                foregroundView = viewHolder.binding.mainPayment
                getDefaultUIUtil().onDraw(
                    c, recyclerView, foregroundView, dX / 2, dY / 2,
                    actionState, isCurrentlyActive
                )
            }
        }
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        listener.onSwiped(viewHolder, direction, viewHolder.adapterPosition)
    }

    interface RecyclerItemTouchHelperListener {
        fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int, position: Int)
    }
}