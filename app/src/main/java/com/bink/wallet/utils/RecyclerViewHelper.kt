package com.bink.wallet.utils

import android.view.View
import android.view.ViewTreeObserver
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView

class RecyclerViewHelper {

    private lateinit var footerListener: ViewTreeObserver.OnGlobalLayoutListener

    fun handleFooterFadeEffect(
        footerViews: List<View?>,
        recyclerView: RecyclerView?,
        gradientView: View?,
        needsFooterPadding: Boolean,
        quotient: Int
    ) {
        var footerMargin = 0
        var footerHeight = 0
        val fadingViewHeightQuotient = 2

        footerViews.forEach { footerView ->
            val footerParams = footerView?.layoutParams as ConstraintLayout.LayoutParams
            footerMargin = +footerParams.bottomMargin
            footerHeight = +footerView.height
            footerView.bringToFront()
        }

        val recyclerParams = recyclerView?.layoutParams as ConstraintLayout.LayoutParams
        val listMargin = quotient * recyclerParams.bottomMargin

        var totalRecyclerBottomPadding =
            (footerMargin + footerHeight + listMargin)

        val fadingViewHeight = (totalRecyclerBottomPadding * fadingViewHeightQuotient)

        if (needsFooterPadding) {
            totalRecyclerBottomPadding = fadingViewHeight
        }
        recyclerView.setPadding(
            0,
            0,
            0,
            totalRecyclerBottomPadding
        )
        val viewParams = gradientView?.layoutParams
        viewParams?.height = fadingViewHeight
        gradientView?.layoutParams = viewParams
    }

    fun setFooterFadeEffect(
        footerViews: List<View?>,
        recyclerView: RecyclerView?,
        gradientView: View?,
        needsFooterPadding: Boolean,
        quotient: Int
    ) {
        // The padding of the list must equate to the size of the CTA (incl. any margins).
        footerListener = ViewTreeObserver.OnGlobalLayoutListener {
            handleFooterFadeEffect(
                footerViews,
                recyclerView,
                gradientView,
                needsFooterPadding,
                quotient
            )
        }
    }

    fun removeFooterListener(container: View) {
        container.viewTreeObserver.removeOnGlobalLayoutListener(footerListener)
    }

    fun registerFooterListener(container: View) {
        container.viewTreeObserver.addOnGlobalLayoutListener(footerListener)
    }
}