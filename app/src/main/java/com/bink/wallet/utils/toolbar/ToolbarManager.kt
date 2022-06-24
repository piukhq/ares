package com.bink.wallet.utils.toolbar

import android.view.MenuItem
import com.bink.wallet.R

class ToolbarManager constructor(
    private var builder: FragmentToolbar
) {
    fun prepareToolbar() {
        builder.apply {
            if (resId != FragmentToolbar.NO_TOOLBAR) {
                toolbar?.let {
                    if (title != -1) {
                        it.setTitle(title)
                    }
                    if (menuId != -1) {
                        it.inflateMenu(menuId)
                    }
                    if (menuItems.isNotEmpty() &&
                        menuClicks.isNotEmpty()
                    ) {
                        for ((index, menuItemId) in menuItems.withIndex()) {
                            (it.menu.findItem(menuItemId) as MenuItem).setOnMenuItemClickListener(menuClicks[index])
                        }
                    }
                    activity?.let { activity ->
                        it.apply {
                            setNavigationIcon(R.drawable.ic_back)
                            setNavigationOnClickListener {
                                activity.onBackPressed()
                            }
                        }
                    }
                }
            }
        }
    }
}