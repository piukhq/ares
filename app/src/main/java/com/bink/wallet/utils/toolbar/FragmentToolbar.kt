package com.bink.wallet.utils.toolbar

import android.view.MenuItem
import android.widget.Toolbar
import androidx.annotation.IdRes
import androidx.annotation.MenuRes
import androidx.annotation.StringRes
import androidx.fragment.app.FragmentActivity

class FragmentToolbar(
    @IdRes val resId: Int,
    @StringRes val title: Int,
    @MenuRes val menuId: Int,
    val menuItems: MutableList<Int>,
    val menuClicks: MutableList<MenuItem.OnMenuItemClickListener?>,
    var activity: FragmentActivity?,
    var toolbar: Toolbar? = null
) {
    class Builder {
        private var resId: Int = 0
        private var menuId: Int = -1
        private var title: Int = -1
        private var menuItems: MutableList<Int> = mutableListOf()
        private var menuClicks: MutableList<MenuItem.OnMenuItemClickListener?> = mutableListOf()
        private var activity: FragmentActivity? = null
        private var toolbar: Toolbar? = null

        fun withId(@IdRes resId: Int) = apply { this.resId = resId }

        fun withTitle(title: Int) = apply { this.title = title }

        fun withMenu(@MenuRes menuId: Int) = apply { this.menuId = menuId }

        fun shouldDisplayBack(activity: FragmentActivity) = apply { this.activity = activity }

        fun with(toolbar: Toolbar?) = apply { this.toolbar = toolbar }

        fun withMenuItems(
            menuItems: MutableList<Int>,
            menuClicks: MutableList<MenuItem.OnMenuItemClickListener?>
        ) = apply {
            this.menuItems.addAll(menuItems)
            this.menuClicks.addAll(menuClicks)
        }

        fun build() =
            FragmentToolbar(resId, title, menuId, menuItems, menuClicks, activity, toolbar)
    }

    companion object {
        const val NO_TOOLBAR = -1
    }
}