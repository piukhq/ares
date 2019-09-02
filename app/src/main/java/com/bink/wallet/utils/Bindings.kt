package com.bink.wallet.utils

import android.graphics.Color
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bink.wallet.LoyaltyCardHeader
import com.bink.wallet.ModalBrandHeader
import com.bink.wallet.R
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.AddFields
import com.bink.wallet.model.response.membership_plan.AuthoriseFields
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bumptech.glide.Glide
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.journeyapps.barcodescanner.BarcodeEncoder


@BindingAdapter("imageUrl")
fun ImageView.loadImage(item: MembershipPlan) {
    if (item.images != null && item.images.isNotEmpty())
        Glide.with(context).load(item.images.first { it.type == 3 }.url).into(this)
}


@BindingAdapter("image")
fun ImageView.setImage(url: String) {
    Glide.with(context).load(url).into(this)
}


@BindingAdapter("isVisible")
fun View.setVisible(isVisible: Boolean) {
    visibility = if (isVisible) {
        View.VISIBLE
    } else {
        View.GONE
    }
}

data class BarcodeWrapper(val barcode: String?, val barcodeType: Int)

@BindingAdapter("barcode")
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

@BindingAdapter("membershipPlan")
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
    plan.account?.plan_name_card?.let {
        binding.loyaltyScheme.text =
            resources.getString(R.string.loyalty_info, plan.account.plan_name_card)
    }
}


@BindingAdapter("membershipCard")
fun LoyaltyCardHeader.linkCard(card: MembershipCard?) {
    if (!card?.getHeroImage()?.url.isNullOrEmpty()) {
        binding.image.setImage(card?.getHeroImage()?.url.toString())
    } else {
        binding.image.setBackgroundColor(Color.GREEN)
    }
    binding.tapCard.setVisible(card?.card?.barcode != null)
}


@BindingAdapter("addField", "authField")
fun TextView.title(addFields: AddFields?, authoriseFields: AuthoriseFields?) {
    if (!addFields?.column.isNullOrEmpty()) {
        this.text = addFields?.column
    }
    if (!authoriseFields?.column.isNullOrEmpty()) {
        this.text = authoriseFields?.column
    }
}

@BindingAdapter("addField", "authField")
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
