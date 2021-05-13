package com.bink.wallet.scenes.browse_brands

import android.widget.Button
import androidx.databinding.BindingAdapter
import com.bink.wallet.R

@BindingAdapter("isOpen")
fun Button.isOpen(isSelected: Boolean) {
    if (!isSelected) {
        setTextColor(resources.getColor(R.color.browse_brands_filters_text_color))
    } else {
        setTextColor(resources.getColor(R.color.black))
    }
}