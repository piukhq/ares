package com.bink.wallet.utils

import android.graphics.Color
import android.os.Parcelable
import android.text.format.DateFormat
import android.text.format.DateUtils
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.bink.wallet.LoyaltyCardHeader
import com.bink.wallet.ModalBrandHeader
import com.bink.wallet.R
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_card.MembershipTransactions
import com.bink.wallet.model.response.membership_plan.AddFields
import com.bink.wallet.model.response.membership_plan.AuthoriseFields
import com.bink.wallet.model.response.membership_plan.EnrolFields
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.utils.enums.LoginStatus
import com.bumptech.glide.Glide
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.android.parcel.Parcelize
import kotlin.math.absoluteValue

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

@Parcelize
data class BarcodeWrapper(val barcode: String?, val barcodeType: Int) : Parcelable

@BindingAdapter("barcode")
fun ImageView.loadBarcode(barcode: BarcodeWrapper?) {
    if (!barcode?.barcode.isNullOrEmpty()) {
        val multiFormatWriter = MultiFormatWriter()
        val heightPx = context.toPixelFromDip(80f)
        val widthPx = context.toPixelFromDip(320f)
        var format: BarcodeFormat? = null
        when (barcode?.barcodeType) {
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
            multiFormatWriter.encode(barcode?.barcode, format, widthPx.toInt(), heightPx.toInt())
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
    if (card?.getHeroImage() != null && card.getHeroImage()?.url != null) {
        binding.image.setImage(card.getHeroImage()?.url.toString())
    } else {
        binding.image.setBackgroundColor(Color.GREEN)
    }
    binding.tapCard.setVisible(card?.card?.barcode != null)
}


@BindingAdapter("addField", "authField", "enrolField")
fun TextView.title(
    addFields: AddFields?,
    authoriseFields: AuthoriseFields?,
    enrolFields: EnrolFields?
) {
    if (!addFields?.column.isNullOrEmpty()) {
        this.text = addFields?.column
    }
    if (!authoriseFields?.column.isNullOrEmpty()) {
        this.text = authoriseFields?.column
    }
    if (!enrolFields?.column.isNullOrEmpty()) {
        this.text = enrolFields?.column
    }
}

@BindingAdapter("addField", "authField", "enrolField")
fun Spinner.setValues(
    addFields: AddFields?,
    authoriseFields: AuthoriseFields?,
    enrolFields: EnrolFields?
) {
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
    if (enrolFields != null && !enrolFields.choice.isNullOrEmpty())
        this.adapter = ArrayAdapter(
            this.context,
            android.R.layout.simple_spinner_dropdown_item,
            enrolFields.choice
        )
}

@BindingAdapter("transactionValue")
fun TextView.setValue(membershipTransactions: MembershipTransactions) {
    val value = membershipTransactions.amounts?.get(0)?.value!!
    val sign: String

    when {
        value < 0 -> {
            sign = "-"
            this.setTextColor(ContextCompat.getColor(context, R.color.black))
        }
        value == 0.0 -> {
            sign = " "
            this.setTextColor(ContextCompat.getColor(context, R.color.amber_pending))
        }
        else -> {
            sign = "+"
            this.setTextColor(ContextCompat.getColor(context, R.color.green_ok))
        }
    }

    val currentValue = membershipTransactions.amounts[0].value?.absoluteValue

    if (membershipTransactions.amounts[0].prefix != null)
        this.text =
            "$sign ${membershipTransactions.amounts[0].prefix} $currentValue"
    else if (membershipTransactions.amounts[0].suffix != null)
        this.text =
            "$sign $currentValue ${membershipTransactions.amounts[0].suffix}"
}

@BindingAdapter("transactionTime")
fun TextView.setTimestamp(timeStamp: Long) {
    this.text = DateFormat.format("dd MMMM yyyy", timeStamp * 1000).toString()
}

@BindingAdapter("transactionArrow")
fun TextView.setArrow(membershipTransactions: MembershipTransactions) {
    val value = membershipTransactions.amounts?.get(0)?.value!!

    when {
        value < 0 -> {
            this.setTextColor(ContextCompat.getColor(context, R.color.black))
        }
        value == 0.0 -> {
            this.setTextColor(ContextCompat.getColor(context, R.color.amber_pending))
            this.text = context.getString(R.string.arrow_left)
        }
        else -> {
            this.setTextColor(ContextCompat.getColor(context, R.color.green_ok))
            this.text = context.getString(R.string.up_arrow)
        }
    }
}

@BindingAdapter("cardTimestamp", "loginStatus")
fun TextView.timeElapsed(card: MembershipCard?, loginStatus: LoginStatus?) {

    when (loginStatus) {
        LoginStatus.STATUS_LOGGED_IN_HISTORY_UNAVAILABLE -> {
            if (card != null && card.balances.isNullOrEmpty()) {
                var elapsed =
                    (System.currentTimeMillis() / 1000 - card.balances?.first()?.updated_at!!) / 60
                var suffix = MINUTES
                if (elapsed >= 60) {
                    elapsed /= 60
                    suffix = HOURS
                    if (elapsed >= 24) {
                        elapsed /= 24
                        suffix = DAYS
                        if (elapsed >= 7) {
                            elapsed /= 7
                            suffix = WEEKS
                            if (elapsed >= 5) {
                                elapsed /= 5
                                suffix = MONTHS
                                if (elapsed >= 12) {
                                    elapsed /= 12
                                    suffix = YEARS
                                }
                            }
                        }
                    }
                }
                this.text = this.context.getString(
                    R.string.transaction_not_supported_description,
                    elapsed.toInt().toString(),
                    suffix
                )
            }
        }
        LoginStatus.STATUS_LOGIN_UNAVAILABLE ->
            this.text =
                this.context.getString(R.string.description_login_unavailable)
        LoginStatus.STATUS_LOGIN_PENDING ->
            this.text = this.context.getString(R.string.description_text)
        LoginStatus.STATUS_SIGN_UP_PENDING ->
            this.text = this.context.getString(R.string.description_text)
        else -> this.text = this.context.getString(R.string.description_text)
    }
}

@BindingAdapter("loginStatus")
fun TextView.setTitleLoginStatus(loginStatus: LoginStatus?) {

    when (loginStatus) {
        LoginStatus.STATUS_LOGGED_IN_HISTORY_UNAVAILABLE -> this.text =
            this.context.getString(R.string.transaction_not_supported_title)
        LoginStatus.STATUS_LOGIN_UNAVAILABLE -> this.text =
            this.context.getString(R.string.transaction_history_not_supported)
        LoginStatus.STATUS_LOGIN_PENDING -> this.text =
            this.context.getString(R.string.log_in_pending)
        LoginStatus.STATUS_SIGN_UP_PENDING -> this.text =
            this.context.getString(R.string.sign_up_pending)
        else -> this.text = this.context.getString(R.string.register_gc_pending)
    }
}


