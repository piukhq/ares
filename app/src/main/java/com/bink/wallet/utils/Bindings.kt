package com.bink.wallet.utils

import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Parcelable
import android.view.View
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.bink.wallet.LoyaltyCardHeader
import com.bink.wallet.ModalBrandHeader
import com.bink.wallet.R
import com.bink.wallet.model.MembershipCardListWrapper
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_card.MembershipTransactions
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.model.response.membership_plan.PlanField
import com.bink.wallet.model.response.payment_card.PaymentCard
import com.bink.wallet.scenes.loyalty_wallet.BarcodeViewModel
import com.bink.wallet.utils.enums.ImageType
import com.bink.wallet.utils.enums.LoginStatus
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.android.parcel.Parcelize
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.absoluteValue


@BindingAdapter("imageUrl")
fun ImageView.loadImage(item: MembershipPlan?) {
    if (!item?.images.isNullOrEmpty()) {
        visibility = View.VISIBLE
        // wrapped in a try/catch as it was throwing error on very strange situations
        try {
            Glide.with(context)
                .load(getIconTypeFromPlan(item))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(this)
        } catch (e: NoSuchElementException) {
            logError("loadImage", e.localizedMessage, e)
        }
    } else {
        visibility = View.INVISIBLE
    }
}

fun getIconTypeFromPlan(item: MembershipPlan?) =
    item?.images?.first { it.type == ImageType.ICON.type }?.url

@BindingAdapter("imageUrl")
fun ImageView.loadImage(item: MembershipCard?) {
    if (!item?.images.isNullOrEmpty()) {
        visibility = View.VISIBLE
        // wrapped in a try/catch as it was throwing error on very strange situations
        try {
            Glide.with(context)
                .load(getIconTypeFromCard(item))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(this)
        } catch (e: NoSuchElementException) {
            logError("loadImage", e.localizedMessage, e)
        }
    } else {
        visibility = View.INVISIBLE
    }
}

fun getIconTypeFromCard(item: MembershipCard?) =
    item?.images?.first { it.type == ImageType.ICON.type }?.url

@BindingAdapter("image")
fun ImageView.setPaymentCardImage(item: PaymentCard) {
    if (!item.images.isNullOrEmpty()) {
        Glide.with(context).load(item.images.first().url).into(this)
    }
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
data class BarcodeWrapper(val membershipCard: MembershipCard?) : Parcelable

@BindingAdapter("membershipCard", "viewModel", requireAll = false)
fun ImageView.loadBarcode(membershipCard: BarcodeWrapper?, viewModel: BarcodeViewModel?) {
    if (!membershipCard?.membershipCard?.card?.barcode.isNullOrEmpty()) {
        val multiFormatWriter = MultiFormatWriter()
        val heightPx = context.toPixelFromDip(80f)
        val widthPx = context.toPixelFromDip(320f)
        var format: BarcodeFormat? = null
        var shouldShowBarcodeImage = true
        val barcodeNumberLength = membershipCard?.membershipCard?.card?.barcode?.length
        val EAN_13_BARCODE_LENGTH_LIMIT = 12

        when (membershipCard?.membershipCard?.card?.barcode_type) {
            0, null -> format = BarcodeFormat.CODE_128
            1 -> format = BarcodeFormat.QR_CODE
            2 -> format = BarcodeFormat.AZTEC
            3 -> format = BarcodeFormat.PDF_417
            4 -> format = BarcodeFormat.EAN_13
            5 -> format = BarcodeFormat.DATA_MATRIX
            6 -> format = BarcodeFormat.ITF
            7 -> format = BarcodeFormat.CODE_39
        }
        membershipCard?.membershipCard?.card?.barcode?.let { barcode ->
            barcodeNumberLength?.let {
                when (format) {
                    BarcodeFormat.ITF -> {
                        // For the ITF barcode format, the library will cause a crash if trying to generate a barcode
                        // that contains letters or has an uneven length
                        shouldShowBarcodeImage = !(barcodeNumberLength.rem(2) != 0 ||
                                barcode.contains(LETTER_REGEX))
                    }
                    BarcodeFormat.EAN_13 -> {
                        // For the EAN_13 barcode format, the library will cause a crash if trying to generate a barcode
                        // that has a length below or above the specified limits
                        shouldShowBarcodeImage = barcodeNumberLength == EAN_13_BARCODE_LENGTH_LIMIT
                    }
                    else -> {
                    }
                }
            }
        }

        if (shouldShowBarcodeImage) {
            try {
                val bitMatrix: BitMatrix =
                    multiFormatWriter.encode(
                        membershipCard?.membershipCard?.card?.barcode,
                        format,
                        widthPx.toInt(),
                        heightPx.toInt()
                    )
                val barcodeEncoder = BarcodeEncoder()
                val bitmap = barcodeEncoder.createBitmap(bitMatrix)
                setImageBitmap(bitmap)
                shouldShowMessage(viewModel, false)
            } catch (e: Exception) {
                visibility = View.GONE
                shouldShowMessage(viewModel, true)

            }

        } else {
            visibility = View.GONE
            shouldShowMessage(viewModel, true)
        }
    }
}

fun View.shouldShowMessage(viewModel: BarcodeViewModel?, showMessage: Boolean) {
    viewModel?.shouldShowLabel?.value = showMessage
}

@BindingAdapter("membershipPlan")
fun ModalBrandHeader.linkPlan(plan: MembershipPlan?) {
    binding.brandImage.loadImage(plan)
    plan?.account?.plan_name?.let {
        binding.loyaltyScheme.text =
            resources.getString(R.string.loyalty_info, plan.account.plan_name)
    }
}

@BindingAdapter("joinCardTitle")
fun TextView.planTitle(plan: MembershipPlan?) {
    text = plan?.account?.plan_name ?: resources.getString(R.string.payment_join_title)
}

@BindingAdapter("joinCardImage")
fun ImageView.image(plan: MembershipPlan?) {
    if (plan == null) {
        setImageResource(R.drawable.ic_no_payment_card)
    } else {
        try {
            Glide.with(context)
                .load(getIconTypeFromPlan(plan))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(this)
        } catch (e: NoSuchElementException) {
            logError("loadImage", e.localizedMessage, e)
        }
    }
}


@BindingAdapter("membershipCard", "membershipPlan", requireAll = true)
fun LoyaltyCardHeader.linkCard(card: MembershipCard?, plan: MembershipPlan?) {
    binding.container.setBackgroundColor(Color.parseColor(card?.card?.colour))
    binding.cardPlaceholderText.text = context.getString(
        R.string.loyalty_card_details_header_placeholder_text,
        plan?.account?.company_name
    )

    var brandImage: String? = card?.getHeroImage()?.url
    card?.isAuthorised()?.let { safeAuthorised ->
        if (safeAuthorised) {
            card.getTierImage()?.let { safeTierImage ->
                brandImage = safeTierImage.url
            }
        }
    }
    Glide.with(context)
        .load(brandImage)
        .listener(object : RequestListener<Drawable> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable>?,
                isFirstResource: Boolean
            ): Boolean = false

            override fun onResourceReady(
                resource: Drawable?,
                model: Any?,
                target: Target<Drawable>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                binding.container.layoutParams = FrameLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                (binding.brandImage.layoutParams as ConstraintLayout.LayoutParams)
                    .dimensionRatio = "${resource?.minimumWidth}:${resource?.minimumHeight}"
                binding.container.setBackgroundColor(Color.TRANSPARENT)
                binding.cardPlaceholderText.visibility = View.GONE
                return false
            }

        })
        .into(binding.image)

    binding.tapCard.text = when {
        !card?.card?.barcode.isNullOrEmpty() -> {
            context.getString(R.string.tap_card_to_show_barcode)
        }
        !card?.card?.membership_id.isNullOrEmpty() -> {
            context.getString(R.string.tap_card_to_show_card_number)
        }
        else -> {
            binding.tapCard.visibility = View.GONE
            EMPTY_STRING
        }
    }
}

@BindingAdapter("textBalance")
fun TextView.textBalance(card: MembershipCard?) {
    val vouchers = card?.vouchers
    if (!vouchers.isNullOrEmpty()) {
        val voucher = vouchers.first()
        text = context.displayVoucherEarnAndTarget(voucher)
    } else {
        if (!card?.balances.isNullOrEmpty()) {
            val balance = card?.balances?.first()
            text = when (balance?.prefix != null) {
                true -> balance?.prefix?.plus(balance.value)
                else -> {
                    balance?.value.plus(balance?.suffix)
                }
            }
        }
    }
}

@BindingAdapter("planField")
fun TextView.title(
    planField: PlanField?
) {
    if (!planField?.column.isNullOrEmpty()) {
        this.text = planField?.column
    }
}

@BindingAdapter("planField")
fun Spinner.setValues(
    planField: PlanField?
) {
    if (planField != null && !planField.choice.isNullOrEmpty())
        adapter = ArrayAdapter(
            context,
            android.R.layout.simple_spinner_dropdown_item,
            planField.choice
        )
}

@BindingAdapter("transactionValue")
fun TextView.setValue(membershipTransactions: MembershipTransactions) {
    val sign: String
    membershipTransactions.amounts?.get(0)?.value?.let {
        when {
            it < 0 -> {
                sign = "-"
                setTextColor(ContextCompat.getColor(context, R.color.black))
            }
            it == 0.0 -> {
                sign = " "
                setTextColor(ContextCompat.getColor(context, R.color.amber_pending))
            }
            else -> {
                sign = "+"
                setTextColor(ContextCompat.getColor(context, R.color.green_ok))
            }
        }
        val currentValue = it.absoluteValue.toInt()

        if (membershipTransactions.amounts[0].prefix != null)
            text =
                resources.getString(
                    R.string.transactions_prefix,
                    sign,
                    membershipTransactions.amounts[0].prefix,
                    currentValue.toString()
                )
        else if (membershipTransactions.amounts[0].suffix != null)
            text = resources.getString(
                R.string.transactions_suffix,
                sign,
                currentValue.toString(),
                membershipTransactions.amounts[0].currency
            )
    }
}

@BindingAdapter("transactionTime")
fun TextView.setTimestamp(transaction: MembershipTransactions) {
    if (transaction.timestamp != null &&
        transaction.description != null
    ) {
        with(this) {
            visibility = View.VISIBLE
            text =
                "${dateFormatTransactionTime(transaction.timestamp)}, ${transaction.description}"
        }
    }
}

@BindingAdapter("transactionTime", "format", "shortMonth")
fun TextView.setTimestamp(timeStamp: Long, format: String = "%s", shortMonth: Boolean = false) {
    with(this) {
        visibility = View.VISIBLE
        text = String.format(format, dateFormatTransactionTime(timeStamp, shortMonth))
    }
}

private fun dateFormatTransactionTime(timeStamp: Long, shortMonth: Boolean = false) =
    SimpleDateFormat(
        getDateFormat(shortMonth),
        Locale.ENGLISH
    ).format(timeStamp * ONE_THOUSAND).toString()

private fun getDateFormat(shortMonth: Boolean): String {
    val builder = StringBuilder("dd MMM")
    if (shortMonth)
        builder.append("M")
    builder.append(" yyyy")
    return builder.toString()
}

@BindingAdapter("transactionArrow")
fun ImageView.setArrow(membershipTransactions: MembershipTransactions) {
    membershipTransactions.amounts?.get(0)?.value?.let {
        when {
            it < 0 -> {
                rotation = 180f
                setColorFilter(
                    ContextCompat.getColor(context, R.color.black),
                    PorterDuff.Mode.SRC_IN
                )
            }
            it == 0.0 -> {
                rotation = -90f
                setColorFilter(
                    ContextCompat.getColor(context, R.color.amber_pending),
                    PorterDuff.Mode.SRC_IN
                )
            }
            else -> {
                setColorFilter(
                    ContextCompat.getColor(context, R.color.green_ok),
                    PorterDuff.Mode.SRC_IN
                )
            }
        }
    }
}

fun TextView.textAndShow(string: String?) {
    string?.let {
        visibility = View.VISIBLE
        text = it
    }
}

@BindingAdapter("backgroundGradient")
fun ConstraintLayout.setBackgroundGradient(paymentCard: PaymentCard) {
    paymentCard.card?.provider?.getCardType()?.let {
        setBackgroundResource(it.background)
    }
}

@BindingAdapter("linkedStatusPaymentCard", "linkStatusMembershipCards", requireAll = true)
fun ImageView.setLinkedStatus(
    paymentCard: PaymentCard,
    membershipCards: MembershipCardListWrapper
) {
    setImageResource(
        if (PaymentCardUtils.existLinkedMembershipCards(
                paymentCard,
                membershipCards.membershipCards
            )
        ) {
            R.drawable.ic_linked
        } else {
            R.drawable.ic_unlinked
        }
    )
}

@BindingAdapter("linkedStatusPaymentCard", "linkStatusMembershipCards", requireAll = true)
fun TextView.setLinkedStatus(paymentCard: PaymentCard, membershipCards: MembershipCardListWrapper) {
    val linkedCardsNumber = PaymentCardUtils.countLinkedPaymentCards(
        paymentCard,
        membershipCards.membershipCards
    )

    text = if (PaymentCardUtils.existLinkedMembershipCards(
            paymentCard,
            membershipCards.membershipCards
        )
    ) {
        context.getString(
            when (linkedCardsNumber) {
                1 -> R.string.payment_card_linked_status
                else -> R.string.payment_cards_linked_status
            },
            linkedCardsNumber
        )
    } else {
        context.getString(R.string.payment_card_not_linked)
    }
}

@BindingAdapter("paymentCardLogo")
fun ImageView.setPaymentCardLogo(paymentCard: PaymentCard) {
    paymentCard.card?.provider?.getCardType()?.let {
        setBackgroundResource(it.logo)
    }
}

@BindingAdapter("paymentCardSubLogo")
fun ImageView.setPaymentCardSubLogo(paymentCard: PaymentCard) {
    paymentCard.card?.provider?.getCardType()?.let {
        setBackgroundResource(it.subLogo)
    }
}

@BindingAdapter("loginStatus")
fun TextView.setTitleLoginStatus(loginStatus: LoginStatus?) {
    text = when (loginStatus) {
        LoginStatus.STATUS_LOGGED_IN_HISTORY_UNAVAILABLE -> this.context.getString(R.string.transaction_not_supported_title)
        LoginStatus.STATUS_LOGIN_UNAVAILABLE -> this.context.getString(R.string.transaction_history_not_supported)
        LoginStatus.STATUS_PENDING -> this.context.getString(R.string.card_status_pending)
        else -> this.context.getString(R.string.empty_string)
    }
}

@BindingAdapter("paymentCardDetailsTitle")
fun TextView.setPcdTitle(hasAddedPlls: Boolean) {
    text = if (hasAddedPlls) {
        context.getString(R.string.payment_card_details_title_text)
    } else {
        context.getString(R.string.payment_card_details_title_text_empty)
    }
}

@BindingAdapter("paymentCardDetailsSubtitle")
fun TextView.setPcdSubtitle(hasAddedPlls: Boolean) {
    text = if (hasAddedPlls) {
        context.getString(R.string.payment_card_details_description_text)
    } else {
        context.getString(R.string.payment_card_details_description_text_empty)
    }
}

@BindingAdapter("pllDescription")
fun TextView.setPllDescription(planNameCard: String?) {
    text = resources.getString(R.string.pll_description, planNameCard)
}

@BindingAdapter("preferenceLabel", "preferenceSlug", requireAll = true)
fun TextView.setPreferenceLabel(preferenceLabel: String?, preferenceSlug: String?) {
    text = if (preferenceSlug == PREFERENCE_MARKETING_SLUG) {
        context.getString(R.string.preference_marketing_bink)
    } else {
        preferenceLabel
    }
}