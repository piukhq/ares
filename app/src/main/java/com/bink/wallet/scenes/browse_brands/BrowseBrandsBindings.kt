package com.bink.wallet.scenes.browse_brands

import android.widget.Button
import androidx.databinding.BindingAdapter
import com.bink.wallet.R
import com.google.android.material.color.MaterialColors

@BindingAdapter("isOpen")
fun Button.isOpen(isSelected: Boolean) {
    if (!isSelected) {
        setTextColor(
            resources.getColor(
                R.color.blue_accent,
                null
            )
        )
    } else {
        setTextColor(
            MaterialColors.getColor(context, R.attr.colorOnSurface, resources.getColor(R.color.black, null))
        )
    }
}