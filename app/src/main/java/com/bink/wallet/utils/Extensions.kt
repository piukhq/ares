package com.bink.wallet.utils

import android.app.AlertDialog
import android.content.Context
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.IdRes
import androidx.annotation.IntegerRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import com.bink.wallet.R

fun Context.toPixelFromDip(value: Float) =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, resources.displayMetrics)

fun Context.toPixelFromDip(@IntegerRes resId: Int) =
    toPixelFromDip(resources.getInteger(resId).toFloat())

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

fun Context.displayModalPopup(
    title: String?,
    message: String?,
    okAction: () -> Unit = {},
    buttonText: Int = R.string.ok,
    hasNegativeButton: Boolean = false
) {
    val builder = AlertDialog.Builder(this)

    title?.let {
        builder.setTitle(title)
    }

    message?.let {
        builder.setMessage(message)
    }

    builder.setNeutralButton(buttonText) { _, _ ->
        okAction()
    }

    if (hasNegativeButton) {
        builder.setNegativeButton(R.string.cancel_text) { dialogInterface, _ ->
            dialogInterface.cancel()
        }
    }

    builder.setOnCancelListener {}

    builder.create().show()
}

fun <T> LiveData<T>.observeNonNull(owner: LifecycleOwner, observer: (t: T) -> Unit) {
    this.observe(owner, Observer {
        it?.let(observer)
    })
}

fun Long.getElapsedTime(context: Context): String {
    var elapsed = this / 60
    var suffix = MINUTES
    if (elapsed >= 60) {
        elapsed /= 60
        suffix = HOURS
        if (elapsed >= 24) {
            elapsed /= 24
            suffix = DAYS
            if (elapsed >= 7) {
                elapsed /= 7
                suffix = WEEKS
                if (elapsed >= 5) {
                    elapsed /= 5
                    suffix = MONTHS
                    if (elapsed >= 12) {
                        elapsed /= 12
                        suffix = YEARS
                    }
                }
            }
        }
    }
    return context.getString(R.string.description_last_checked, elapsed.toInt().toString(), suffix)
}

fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

fun String.headerTidy(): String {
    return this
        .replace("=", "")
        .replace("\n", "")
}