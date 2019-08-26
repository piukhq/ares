package com.bink.wallet.utils

import android.app.AlertDialog
import android.content.Context
import android.util.TypedValue
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import com.bink.wallet.R

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

fun Context.displayModalPopup(title: String, message: String) {
    val builder = AlertDialog.Builder(this)
    builder.setTitle(title)
    builder.setMessage(message)
    builder.setNeutralButton(R.string.ok) { _, _ -> }
    builder.create().show()
}