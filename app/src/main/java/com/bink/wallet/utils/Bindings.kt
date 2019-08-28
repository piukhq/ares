package com.bink.wallet.utils

import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import com.bink.wallet.ModalBrandHeader
import com.bink.wallet.R
import com.bink.wallet.model.response.membership_plan.AddFields
import com.bink.wallet.model.response.membership_plan.AuthoriseFields
import com.bink.wallet.model.response.membership_plan.MembershipPlan
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

        val bitMatrix: BitMatrix =
            multiFormatWriter.encode(barcode.barcode, format, widthPx.toInt(), heightPx.toInt())
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
            resources.getString(R.string.plan_description),
            plan.account?.plan_description.toString()
        )
    }
    binding.loyaltyScheme.setOnClickListener {
        context.displayModalPopup(
            resources.getString(R.string.plan_description),
            plan.account?.plan_description.toString()
        )
    }
    binding.loyaltyScheme.text = resources.getString(R.string.loyalty_info, plan.account?.plan_name)
}


@BindingAdapter("bind:addField", "bind:authField")
fun TextView.title(addFields: AddFields?, authoriseFields: AuthoriseFields?) {
    if (!addFields?.column.isNullOrEmpty()) {
        this.text = addFields?.column
    }
    if (!authoriseFields?.column.isNullOrEmpty()) {
        this.text = authoriseFields?.column
    }
}

@BindingAdapter("bind:addField", "bind:authField")
fun Spinner.setValues(addFields: AddFields?, authoriseFields: AuthoriseFields?) {
    if (addFields != null && !addFields.choice.isNullOrEmpty())
        this.adapter = ArrayAdapter(
            this.context,
            android.R.layout.simple_spinner_dropdown_item,
            addFields.choice
        )
    if (authoriseFields != null && !authoriseFields.choice.isNullOrEmpty())
        this.adapter = ArrayAdapter(
            this.context,
            android.R.layout.simple_spinner_dropdown_item,
            authoriseFields.choice
        )
}