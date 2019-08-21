package com.bink.wallet.utils

import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bink.wallet.ModalBrandHeader
import com.bink.wallet.R
import com.bink.wallet.scenes.browse_brands.model.MembershipPlan
import com.bumptech.glide.Glide
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.journeyapps.barcodescanner.BarcodeEncoder

@BindingAdapter("bind:imageUrl")
fun ImageView.loadImage(item: MembershipPlan) {
    Glide.with(context)
        .load(item.images?.filter { it.type == 3 }?.get(0)?.url)
        .into(this)
}

@BindingAdapter("bind:isVisible")
fun View.setVisible(isVisible: Boolean){
    visibility = if(isVisible){
        View.VISIBLE
    } else {
        View.GONE
    }
}

data class BarcodeWrapper(val barcode: String?, val barcodeType: Int)

@BindingAdapter("bind:barcode")
fun ImageView.loadBarcode(barcode: BarcodeWrapper) {
    if (!barcode.barcode.isNullOrEmpty()) {
        val multiFormatWriter = MultiFormatWriter()
        val heightPx = context.toPixelFromDip(80f)
        val widthPx = context.toPixelFromDip(320f)
        var format: BarcodeFormat? = null
        when (barcode.barcodeType) {
            0 -> format = BarcodeFormat.CODE_128
            1 -> format = BarcodeFormat.QR_CODE
            2 -> format = BarcodeFormat.AZTEC
            3 -> format = BarcodeFormat.PDF_417
            4 -> format = BarcodeFormat.EAN_13
            5 -> format = BarcodeFormat.DATA_MATRIX
            6 -> format = BarcodeFormat.ITF
            7 -> format = BarcodeFormat.CODE_39
        }

        val bitMatrix: BitMatrix = multiFormatWriter.encode(barcode.barcode, format, widthPx.toInt(), heightPx.toInt())
        val barcodeEncoder = BarcodeEncoder()
        val bitmap = barcodeEncoder.createBitmap(bitMatrix)
        setImageBitmap(bitmap)
    }
}

@BindingAdapter("bind:membershipPlan")
fun ModalBrandHeader.linkPlan(plan: MembershipPlan) {
    binding.brandImage.loadImage(plan)
    binding.brandImage.setOnClickListener {
        context.displayModalPopup(
            plan, resources.getString(R.string.plan_description),
            plan.account?.plan_description.toString()
        )
    }
    binding.loyaltyScheme.setOnClickListener {
        context.displayModalPopup(
            plan, resources.getString(R.string.plan_description),
            plan.account?.plan_description.toString()
        )
    }
    binding.loyaltyScheme.text = resources.getString(R.string.loyalty_info, plan.account?.plan_name)
}

