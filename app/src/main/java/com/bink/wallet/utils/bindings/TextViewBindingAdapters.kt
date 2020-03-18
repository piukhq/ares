package com.bink.wallet.utils.bindings

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bink.wallet.R
import com.bink.wallet.model.response.membership_card.Burn
import com.bink.wallet.model.response.membership_card.Earn

@BindingAdapter("showNumberOrBarcodeDescription")
fun TextView.setNumberOrBarcodeDescription(isBarcodeAvailable: Boolean) {
    text = if (isBarcodeAvailable) {
        context.getString(R.string.barcode_description)
    } else {
        context.getString(R.string.card_number_description)
    }
}

@BindingAdapter("voucherSubtext", "voucherTargetValue", requireAll = true)
fun TextView.setVoucherSubText(subtext: String?, targetValue: Float) {
    text = context.getString(R.string.voucher_stamp_subtext, targetValue.toInt())
}

@BindingAdapter("voucherBurn")
fun TextView.setVoucherTitle(voucherBurn: Burn?) {
    text = context.getString(
        R.string.voucher_stamp_title,
        voucherBurn?.prefix, voucherBurn?.suffix
    )
}

@BindingAdapter("voucherEarn")
fun TextView.setVoucherCollectedProgress(voucherEarn: Earn) {
    text = context.getString(
        R.string.voucher_stamp_collected,
        voucherEarn.value?.toInt(),
        voucherEarn.target_value?.toInt()
    )
}