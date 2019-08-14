package com.bink.wallet.utils

import android.content.Context
import android.util.DisplayMetrics
import android.util.TypedValue

/**
 * Applies density dimensions to the [Float]
 */
fun Float.toDp(displayMetrics: DisplayMetrics) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, displayMetrics)

fun Context.toPixelFromDip(value: Float) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, resources.displayMetrics)