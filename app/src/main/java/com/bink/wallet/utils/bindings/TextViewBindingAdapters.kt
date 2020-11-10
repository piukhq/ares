package com.bink.wallet.utils.bindings

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bink.wallet.R
import com.bink.wallet.model.response.membership_card.Burn
import com.bink.wallet.model.response.membership_card.Earn
import com.bink.wallet.model.response.membership_card.Voucher
import com.bink.wallet.utils.UtilFunctions
import com.bink.wallet.utils.ValueDisplayUtils
import com.bink.wallet.utils.enums.VoucherStates

const val ACCUMULATOR = "accumulator"
const val STAMP = "stamps"

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
fun TextView.setVoucherHeadline(voucher: Voucher) {
    voucher.state?.let { state ->
        text = when (state) {
            VoucherStates.REDEEMED.state,
            VoucherStates.EXPIRED.state,
            VoucherStates.CANCELLED.state
            -> state.capitalize()
            VoucherStates.ISSUED.state -> context.getString(R.string.earned).capitalize()
            else ->
                if (voucher.earn?.type == STAMP)
                    context.getString(
                        R.string.voucher_stamp_in_progress_headline,
                        ValueDisplayUtils.displayFormattedHeadline(voucher.earn)
                    ) else context.getString(
                    R.string.voucher_accumulator_in_progress_headline,
                    ValueDisplayUtils.displayFormattedHeadline(voucher.earn)
                )


        }
    }

}