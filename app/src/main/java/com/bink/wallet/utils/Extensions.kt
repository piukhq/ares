package com.bink.wallet.utils

import android.content.Context
import android.util.TypedValue

fun Context.toPixelFromDip(value: Float) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, resources.displayMetrics)