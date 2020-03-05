package com.bink.wallet.utils.bindings

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bink.wallet.R

@BindingAdapter("showNumberOrBarcodeDescription")
fun TextView.setNumberOrBarcodeDescription(isBarcodeAvailable: Boolean) {
    text = if (isBarcodeAvailable) {
        context.getString(R.string.barcode_description)
    } else {
        context.getString(R.string.card_number_description)
    }
}