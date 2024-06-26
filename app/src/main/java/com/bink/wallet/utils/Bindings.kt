package com.bink.wallet.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Parcelable
import android.view.View
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.LoyaltyCardHeader
import com.bink.wallet.ModalBrandHeader
import com.bink.wallet.R
import com.bink.wallet.data.SharedPreferenceManager
import com.bink.wallet.model.MembershipCardListWrapper
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_card.MembershipTransactions
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.model.response.membership_plan.PlanField
import com.bink.wallet.model.response.payment_card.PaymentCard
import com.bink.wallet.scenes.loyalty_wallet.barcode.BarcodeViewModel
import com.bink.wallet.scenes.loyalty_wallet.wallet.adapter.RecyclerViewItemDecoration
import com.bink.wallet.utils.enums.CardType
import com.bink.wallet.utils.enums.ImageType
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.mixpanel.android.mpmetrics.MixpanelAPI
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.absoluteValue

private const val CONTACT_US = "Contact us"

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

fun getAlternateHeroTypeFromPlan(item: MembershipPlan?) =
    item?.images?.first { it.type == ImageType.ALTERNATE_HERO.type }?.url

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
        val isSquare = when (membershipCard?.membershipCard?.card?.getBarcodeFormat()) {
            BarcodeFormat.QR_CODE,
            BarcodeFormat.AZTEC,
            -> true
            else -> false
        }
        val heightPx = context.toPixelFromDip(if (isSquare) 100f else 80f)
        val widthPx = context.toPixelFromDip(if (isSquare) 100f else 320f)
        val format = membershipCard?.membershipCard?.card?.getBarcodeFormat()
        var shouldShowBarcodeImage = true
        val barcodeNumberLength = membershipCard?.membershipCard?.card?.barcode?.length

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
                        shouldShowBarcodeImage =
                            (barcodeNumberLength in EAN_13_BARCODE_LENGTH_LIMIT)
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

fun shouldShowMessage(viewModel: BarcodeViewModel?, showMessage: Boolean) {
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
        setImageResource(R.drawable.ic_payment_icon)
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

@BindingAdapter("cardOnBoarding")
fun ImageView.loadAlternateHeroImage(plan: MembershipPlan?) {
    try {
        Glide.with(context)
            .load(getAlternateHeroTypeFromPlan(plan))
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(this)
    } catch (e: NoSuchElementException) {
        logError("loadImage", e.localizedMessage, e)
    }

}

@BindingAdapter("membershipCard", "membershipPlan", requireAll = true)
fun LoyaltyCardHeader.linkCard(card: MembershipCard?, plan: MembershipPlan?) {

    if (plan?.getCardType() != CardType.PLL && !card?.card?.barcode.isNullOrEmpty()) {

        binding.container.visibility = View.GONE


        val cardNumber = card?.card?.membership_id ?: ""
        val barcode = card?.card?.barcode ?: ""

        when (val barcodeFormat = card?.card?.getBarcodeFormat()) {
            BarcodeFormat.QR_CODE,
            BarcodeFormat.AZTEC,
            -> {
                binding.tapCard.text =
                    if (card.card?.getBarcodeFormat() == BarcodeFormat.QR_CODE) context.getString(R.string.tap_to_enlarge_qr) else context.getString(
                        R.string.tap_to_enlarge_aztec
                    )
                binding.sbTitle.text = context.getString(R.string.barcode_card_number)
                binding.sbCompanyLogo.loadImage(plan)
                binding.sbBarcode.loadBarcode(BarcodeWrapper(card), null)
                binding.squareBarcodeContainer.visibility = View.VISIBLE
                binding.sbBarcodeText.text = cardNumber
                binding.sbCopyNumber.setOnClickListener {
                    copyCardNumber(context, cardNumber)
                }
            }
            BarcodeFormat.ITF, BarcodeFormat.EAN_13 -> if (!shouldShowBarcode(
                    barcodeFormat,
                    barcode
                )
            ) loadNoBarcodeState(plan, card) else loadRectangularBarcode(plan, card, cardNumber)
            else -> {
                loadRectangularBarcode(plan, card, cardNumber)
            }
        }

    } else if (plan?.getCardType() != CardType.PLL && card?.card?.barcode.isNullOrEmpty()) {
        loadNoBarcodeState(plan, card)

    } else {
        binding.container.setBackgroundColor(Color.parseColor(card?.card?.colour))

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
            .transition(DrawableTransitionOptions.withCrossFade(500))
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean,
                ): Boolean = false

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean,
                ): Boolean {
                    binding.container.layoutParams = FrameLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    (binding.brandImage.layoutParams as ConstraintLayout.LayoutParams)
                        .dimensionRatio = "${resource?.minimumWidth}:${resource?.minimumHeight}"
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
}

private fun LoyaltyCardHeader.loadRectangularBarcode(
    plan: MembershipPlan?,
    card: MembershipCard?,
    cardNumber: String,
) {
    binding.tapCard.text = context.getString(R.string.tap_to_enlarge_barcode)
    binding.rbTitle.text = context.getString(R.string.barcode_card_number)
    binding.rbCompanyLogo.loadImage(plan)
    binding.rbBarcode.loadBarcode(BarcodeWrapper(card), null)
    binding.rectangleBarcodeContainer.visibility = View.VISIBLE
    binding.rbBarcodeText.text = cardNumber
    binding.rbCopyNumber.setOnClickListener {
        copyCardNumber(context, cardNumber)
    }

}

private fun copyCardNumber(context: Context, cardNumber: String) {
    //Copy to clip board and pop toast
    val clipboard = context.getSystemService(ClipboardManager::class.java)
    val clip = ClipData.newPlainText("Card Number", cardNumber)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_LONG).show()

    val mixpanelKey = if (isProduction()) Keys.mixPanelProductionApiKey() else Keys.mixPanelBetaApiKey()
    val mixpanel = MixpanelAPI.getInstance(context, mixpanelKey)
    mixpanel.track(MixpanelEvents.COPY_CARD)
}

private fun LoyaltyCardHeader.loadNoBarcodeState(
    plan: MembershipPlan?,
    card: MembershipCard?,
) {
    binding.container.visibility = View.GONE
    binding.noBarcodeCompanyLogo.loadAlternateHeroImage(plan)
    binding.noBarcodeCardNumberTitle.text = context.getString(R.string.barcode_card_number)
    binding.noBarcodeCardNumber.text = card?.card?.membership_id ?: ""
    binding.noBarcodeContainer.visibility = View.VISIBLE
    binding.tapCard.text = context.getString(R.string.tap_card_to_show_card_number)
}

private fun shouldShowBarcode(barcodeFormat: BarcodeFormat, barcode: String): Boolean {
    val barcodeNumberLength = barcode.length

    return when (barcodeFormat) {
        BarcodeFormat.ITF -> !(barcodeNumberLength.rem(2) != 0 ||
                barcode.contains(LETTER_REGEX))
        BarcodeFormat.EAN_13 -> (barcodeNumberLength in EAN_13_BARCODE_LENGTH_LIMIT)
        else -> false
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
    planField: PlanField?,
) {
    if (!planField?.column.isNullOrEmpty()) {
        this.text = planField?.column
    }
}

@BindingAdapter("planField")
fun Spinner.setValues(
    planField: PlanField?,
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

fun dateFormatTransactionTime(timeStamp: Long, shortMonth: Boolean = false) =
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
    membershipCards: MembershipCardListWrapper,
) {
    if (paymentCard.isCardActive()) {
        SharedPreferenceManager.hasNoActivePaymentCards = false
        SharedPreferenceManager.isPaymentEmpty = false

        visibility = View.VISIBLE
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
    } else {
        visibility = View.GONE
    }

}

@BindingAdapter("linkedStatusPaymentCard", "linkStatusMembershipCards", requireAll = true)
fun TextView.setLinkedStatus(paymentCard: PaymentCard, membershipCards: MembershipCardListWrapper) {
    if (paymentCard.isCardActive()) {
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
            context.getString(R.string.payment_card_ready_to_link)
        }
    } else {
        text = PaymentCardUtils.cardStatus(paymentCard.status ?: "")
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

@BindingAdapter("paymentCardDetailsTitle", "paymentCard", requireAll = false)
fun TextView.setPcdTitle(hasAddedPlls: Boolean, paymentCard: PaymentCard) {
    text = if (paymentCard.card?.isExpired() == true) {
        context.getString(R.string.pcd_expired_card_title)
    } else if (paymentCard.isCardActive()) {
        if (hasAddedPlls) {
            context.getString(R.string.payment_card_details_title_text)
        } else {
            context.getString(R.string.payment_card_details_title_text_empty)
        }

    } else {
        if (PaymentCardUtils.cardStatus(
                paymentCard.status ?: ""
            ) == PENDING_CARD
        ) context.getString(R.string.payment_card_pending_title_text) else context.getString(R.string.payment_card_inactive_title_text)
    }
}

@BindingAdapter("paymentCardDetailsSubtitle", "paymentCard", "listener", requireAll = false)
fun TextView.setPcdSubtitle(
    hasAddedPlls: Boolean,
    paymentCard: PaymentCard,
    hyperlinkClick: (() -> Unit)?,
) {
    text = if (paymentCard.card?.isExpired() == true) {
        UtilFunctions.buildHyperlinkSpanStringWithoutUrl(
            context.getString(
                R.string.pcd_expired_card_description
            ), CONTACT_US, this, hyperlinkClick
        )
    } else if (paymentCard.isCardActive()) {
        if (hasAddedPlls) {
            context.getString(R.string.payment_card_details_description_text)
        } else {
            context.getString(R.string.payment_card_details_description_text_empty)
        }
    } else {
        if (PaymentCardUtils.cardStatus(
                paymentCard.status ?: ""
            ) == PENDING_CARD
        ) {
            UtilFunctions.buildHyperlinkSpanStringWithoutUrl(
                context.getString(
                    R.string.payment_card_pending_description_text
                ), CONTACT_US, this, hyperlinkClick
            )

        } else {
            UtilFunctions.buildHyperlinkSpanStringWithoutUrl(
                context.getString(R.string.payment_card_inactive_description_text),
                CONTACT_US,
                this,
                hyperlinkClick
            )
        }
    }

}

@BindingAdapter("paymentCardAddedDate")
fun TextView.setPaymentCardAddedDate(paymentCard: PaymentCard) {
    if (paymentCard.isCardActive()) {
        visibility = View.GONE
    } else {
        if (PaymentCardUtils.cardStatus(paymentCard.status ?: "") == PENDING_CARD) {
            visibility = View.VISIBLE
            try {
                text = context.getString(
                    R.string.payment_card_added_date,
                    paymentCard.account?.consents?.get(0)?.timestamp?.let {
                        DateTimeUtils.dateFormatTimeStamp(it)
                    })
            } catch (e: Exception) {
                visibility = View.GONE
            }
        } else {
            visibility = View.GONE
        }
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

@BindingAdapter("itemDecorationSpacing")
fun RecyclerView.setItemDecorationSpacing(spacingPx: Float) {
    addItemDecoration(
        RecyclerViewItemDecoration()
    )
}