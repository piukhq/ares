package com.bink.wallet.utils

import android.content.Context
import android.util.TypedValue
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavDirections

fun Context.toPixelFromDip(value: Float) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, resources.displayMetrics)

fun NavController.navigateIfAdded(fragment: Fragment, @IdRes resId: Int) {
    if (fragment.isAdded) {
        navigate(resId)
    }
}

fun NavController.navigateIfAdded(fragment: Fragment, navDirections: NavDirections) {
    if (fragment.isAdded) {
        navigate(navDirections)
    }
}