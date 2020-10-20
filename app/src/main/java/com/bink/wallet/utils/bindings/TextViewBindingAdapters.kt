package com.bink.wallet.utils.bindings

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bink.wallet.R
import com.bink.wallet.model.response.membership_card.Burn
import com.bink.wallet.model.response.membership_card.Earn
import com.bink.wallet.model.response.membership_card.Voucher
import com.bink.wallet.utils.UtilFunctions
import com.bink.wallet.utils.ValueDisplayUtils

@BindingAdapter("showNumberOrBarcodeDescription")
fun TextView.setNumberOrBarcodeDescription(isBarcodeAvailable: Boolean) {
    text = if (isBarcodeAvailable) {
        context.getString(R.string.barcode_description)
    } else {
        context.getString(R.string.card_number_description)
    }
}

@BindingAdapter("voucherEarnSubtitle", requireAll = true)
fun TextView.setVoucherSubText(voucherEarn: Earn) {
    text = context.getString(
        R.string.voucher_stamp_subtext,
        voucherEarn.target_value?.toInt(),
        voucherEarn.suffix
    )
}

@BindingAdapter("voucherBurn")
fun TextView.setVoucherTitle(voucherBurn: Burn?) {
    text = context.getString(
        R.string.voucher_stamp_title,
        ValueDisplayUtils.displayValue(
            voucherBurn?.value,
            voucherBurn?.prefix,
            voucherBurn?.suffix,
            null
        )
    )
}

@BindingAdapter("voucherEarn")
fun TextView.setVoucherCollectedProgress(voucherEarn: Earn) {
    text = context.getString(
        R.string.voucher_stamp_collected,
        voucherEarn.value?.toInt(),
        voucherEarn.target_value?.toInt(),
        voucherEarn.suffix
    )
}

@BindingAdapter("voucherHeadline")
fun TextView.setVoucherHeadline(voucher:Voucher){
    text = voucher.headline ?: context.getString(R.string.voucher_detail_headline_cancelled)
}