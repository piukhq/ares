package com.bink.wallet.utils

import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bink.wallet.scenes.browse_brands.model.MembershipPlan
import com.bumptech.glide.Glide
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.journeyapps.barcodescanner.BarcodeEncoder

@BindingAdapter("bind:imageUrl")
fun ImageView.loadImage(item: MembershipPlan) {
    Glide.with(context).load(item.images?.first { it.type == 3 }?.url).into(this)
}

@BindingAdapter("bind:isVisible")
fun View.setVisible(isVisible: Boolean) {
    visibility = if (isVisible) {
        View.VISIBLE
    } else {
        View.GONE
    }
}

@BindingAdapter("bind:barcode")
fun ImageView.loadBarcode(barcode: String?) {
    if (!barcode.isNullOrEmpty()) {
        val multiFormatWriter = MultiFormatWriter()
        val heightPx = context.toPixelFromDip(80f)
        val widthPx = context.toPixelFromDip(320f)
        val bitMatrix: BitMatrix =
            multiFormatWriter.encode(barcode, BarcodeFormat.CODE_128, widthPx.toInt(), heightPx.toInt())
        val barcodeEncoder = BarcodeEncoder()
        val bitmap = barcodeEncoder.createBitmap(bitMatrix)
        setImageBitmap(bitmap)
    }
}