package com.bink.wallet.utils

import android.graphics.Color
import android.os.Parcelable
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageView
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
import com.bink.wallet.model.response.membership_plan.PlanFields
import com.bink.wallet.model.response.payment_card.PaymentCard
import com.bink.wallet.utils.enums.ImageType
import com.bink.wallet.utils.enums.LoginStatus
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.android.parcel.Parcelize
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
            Log.e("loadImage", e.localizedMessage, e)
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
            Log.e("loadImage", e.localizedMessage, e)
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

@BindingAdapter("membershipCard")
fun ImageView.loadBarcode(membershipCard: BarcodeWrapper?) {
    if (!membershipCard?.membershipCard?.card?.barcode.isNullOrEmpty()) {
        val multiFormatWriter = MultiFormatWriter()
        val heightPx = context.toPixelFromDip(80f)
        val widthPx = context.toPixelFromDip(320f)
        var format: BarcodeFormat? = null
        when (membershipCard?.membershipCard?.card?.barcode_type) {
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
            multiFormatWriter.encode(
                membershipCard?.membershipCard?.card?.barcode,
                format,
                widthPx.toInt(),
                heightPx.toInt()
            )
        val barcodeEncoder = BarcodeEncoder()
        val bitmap = barcodeEncoder.createBitmap(bitMatrix)
        setImageBitmap(bitmap)
    }
}

@BindingAdapter("membershipPlan")
fun ModalBrandHeader.linkPlan(plan: MembershipPlan?) {
    binding.brandImage.loadImage(plan)
    plan?.account?.plan_name_card?.let {
        binding.loyaltyScheme.text =
            resources.getString(R.string.loyalty_info, plan.account.plan_name_card)
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
            Log.e("loadImage", e.localizedMessage, e)
        }
    }
}


@BindingAdapter("membershipCard")
fun LoyaltyCardHeader.linkCard(card: MembershipCard?) {
    if (card?.getHeroImage() != null && card.getHeroImage()?.url != null) {
        binding.image.setImage(card.getHeroImage()?.url.toString())
    } else {
        binding.image.setBackgroundColor(Color.GREEN)
    }
    if (card?.card?.barcode.isNullOrEmpty()) {
        if (card?.card?.membership_id.isNullOrEmpty()) {
            binding.tapCard.visibility = View.GONE
        } else {
            binding.tapCard.text =
                binding.root.context.getString(R.string.tap_card_to_show_card_number)
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
        val balance = card?.balances?.first()
        if (!card?.balances.isNullOrEmpty()) {
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
    planFields: PlanFields?
) {
    if (!planFields?.column.isNullOrEmpty()) {
        this.text = planFields?.column
    }
}

@BindingAdapter("planField")
fun Spinner.setValues(
    planFields: PlanFields?
) {
    if (planFields != null && !planFields.choice.isNullOrEmpty())
        adapter = ArrayAdapter(
            context,
            android.R.layout.simple_spinner_dropdown_item,
            planFields.choice
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

fun TextView.setTimestamp(timeStamp: Long) {
    this.text = dateFormatTransactionTime(timeStamp)
}

@BindingAdapter("transactionTime", "format", "shortMonth")
fun TextView.setTimestamp(timeStamp: Long, format: String = "%s", shortMonth: Boolean = false) {
    with(this) {
        visibility = View.VISIBLE
        text = String.format(format, dateFormatTransactionTime(timeStamp, shortMonth))
    }
}

private fun dateFormatTransactionTime(timeStamp: Long, shortMonth: Boolean = false) =
    DateFormat.format(getDateFormat(shortMonth), timeStamp * 1000).toString()

private fun getDateFormat(shortMonth: Boolean): String {
    val builder = StringBuilder("dd MMM")
    if (shortMonth)
        builder.append("M")
    builder.append(" yyyy")
    return builder.toString()
}

@BindingAdapter("transactionTime", "format")
fun TextView.setFullTimestamp(timeStamp: Long, format: String = "%s") {
    with(this) {
        visibility = View.VISIBLE
        text = String.format(format, dateTimeFormatTransactionTime(timeStamp))
    }
}

private fun dateTimeFormatTransactionTime(timeStamp: Long) =
    DateFormat.format("dd MMM yyyy HH:mm:ss", timeStamp * 1000).toString()

@BindingAdapter("transactionArrow")
fun TextView.setArrow(membershipTransactions: MembershipTransactions) {
    membershipTransactions.amounts?.get(0)?.value?.let {
        when {
            it < 0 -> {
                setTextColor(ContextCompat.getColor(context, R.color.black))
            }
            it == 0.0 -> {
                setTextColor(ContextCompat.getColor(context, R.color.amber_pending))
                text = context.getString(R.string.arrow_left)
            }
            else -> {
                setTextColor(ContextCompat.getColor(context, R.color.green_ok))
                text = context.getString(R.string.up_arrow)
            }
        }
    }
}

// TODO replace logic
@BindingAdapter("cardTimestamp", "loginStatus")
fun TextView.timeElapsed(card: MembershipCard?, loginStatus: LoginStatus?) {
    when (loginStatus) {
        LoginStatus.STATUS_LOGGED_IN_HISTORY_UNAVAILABLE -> {
            card?.let { membershipCard ->
                if (membershipCard.balances.isNullOrEmpty()) {
                    membershipCard.balances?.first()?.updated_at?.let {
                        var elapsed =
                            (System.currentTimeMillis() / 1000 - it) / NUMBER_SECONDS_IN_MINUTE
                        var suffix = MINUTES
                        if (elapsed >= NUMBER_MINUTES_IN_HOUR) {
                            elapsed /= NUMBER_MINUTES_IN_HOUR
                            suffix = HOURS
                            if (elapsed >= NUMBER_HOURS_IN_DAY) {
                                elapsed /= NUMBER_HOURS_IN_DAY
                                suffix = DAYS
                                if (elapsed >= NUMBER_DAYS_IN_WEEK) {
                                    elapsed /= NUMBER_DAYS_IN_WEEK
                                    suffix = WEEKS
                                    if (elapsed >= NUMBER_WEEKS_IN_MONTH) {
                                        elapsed /= NUMBER_WEEKS_IN_MONTH
                                        suffix = MONTHS
                                        if (elapsed >= NUMBER_MONTHS_IN_YEAR) {
                                            elapsed /= NUMBER_MONTHS_IN_YEAR
                                            suffix = YEARS
                                        }
                                    }
                                }
                            }
                        }
                        text = this.context.getString(
                            R.string.transaction_not_supported_description,
                            elapsed.toInt().toString(),
                            suffix
                        )
                    }

                }
            }
        }
        LoginStatus.STATUS_LOGIN_UNAVAILABLE ->
            text =
                this.context.getString(R.string.description_login_unavailable)
        LoginStatus.STATUS_PENDING ->
            text = this.context.getString(R.string.description_text)
        else -> text = this.context.getString(R.string.empty_string)
    }
}

fun TextView.textAndShow(string: String?) {
    string?.let {
        with(this) {
            visibility = View.VISIBLE
            text = it
        }
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
        when (PaymentCardUtils.existLinkedMembershipCards(
            paymentCard,
            membershipCards.membershipCards
        )) {
            true -> R.drawable.ic_linked
            false -> R.drawable.ic_unlinked
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