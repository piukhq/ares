package com.bink.wallet.utils

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.text.Spannable
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import com.bink.wallet.BuildConfig
import com.bink.wallet.MainActivity
import com.bink.wallet.R
import com.bink.wallet.model.response.membership_card.CardBalance
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.utils.enums.BuildTypes
import org.json.JSONException
import org.json.JSONObject
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.util.*

fun Context.toPixelFromDip(value: Float) =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, resources.displayMetrics)

fun NavController.navigateIfAdded(
    fragment: Fragment, @IdRes resId: Int,
    currentDestinationId: Int? = null
) {
    if (currentDestinationId != null) {
        if (fragment.isAdded && currentDestinationId == currentDestination?.id) {
            navigate(resId)
        }
    } else {
        if (fragment.isAdded) {
            navigate(resId)
        }
    }
}

fun NavController.navigateIfAdded(
    fragment: Fragment,
    navDirections: NavDirections,
    currentDestinationId: Int? = null
) {
    if (currentDestinationId != null) {
        if (fragment.isAdded && currentDestinationId == currentDestination?.id) {
            navigate(navDirections)
        }
    } else {
        if (fragment.isAdded) {
            navigate(navDirections)
        }
    }
}

fun Context.displayModalPopup(
    title: String?,
    message: String?,
    okAction: () -> Unit = {},
    cancelAction: () -> Unit = {},
    buttonText: Int = R.string.ok,
    hasNegativeButton: Boolean = false,
    isCancelable: Boolean = true
) {
    val builder = AlertDialog.Builder(this)

    title?.let {
        builder.setTitle(title)
    }

    message?.let {
        builder.setMessage(message)
    }

    builder.setNeutralButton(buttonText) { _, _ ->
        okAction()
    }

    builder.setOnCancelListener {
        cancelAction()
    }

    builder.setCancelable(isCancelable)

    if (hasNegativeButton) {
        builder.setNegativeButton(R.string.cancel_text) { dialogInterface, _ ->
            dialogInterface.cancel()
        }
    }

    builder.create().show()
}

fun <T> LiveData<T>.observeNonNull(owner: LifecycleOwner, observer: (t: T) -> Unit) {
    this.observe(owner) {
        it?.let(observer)
    }
}

fun LiveData<Exception>.observeErrorNonNull(
    context: Context,
    owner: LifecycleOwner,
    defaultErrorTitle: String,
    defaultErrorMessage: String,
    isUserDriven: Boolean,
    observer: ((t: Exception) -> Unit)?
) {
    this.observe(owner) {
        it?.let {
            if (((it is HttpException)
                        && it.code() >= ApiErrorUtils.SERVER_ERROR)
                || it is SocketTimeoutException
            ) {
                context.displayModalPopup(
                    context.getString(R.string.error_server_down_title),
                    context.getString(R.string.error_server_down_message)
                )
            } else if (UtilFunctions.hasCertificatePinningFailed(it) &&
                isUserDriven
            ) {
                UtilFunctions.showCertificatePinningDialog(context)
            } else {
                if (defaultErrorTitle.isNotEmpty() || defaultErrorMessage.isNotEmpty()) {
                    context.displayModalPopup(
                        defaultErrorTitle,
                        defaultErrorMessage
                    )
                }
            }
        }

        observer?.let { safeObserver ->
            it?.let(safeObserver)
        }
    }
}

fun LiveData<Exception>.observeErrorNonNull(
    context: Context,
    owner: LifecycleOwner,
    isUserDriven: Boolean,
    observer: ((t: Exception) -> Unit)?
) = observeErrorNonNull(context, owner, EMPTY_STRING, EMPTY_STRING, isUserDriven, observer)

fun LiveData<Exception>.observeErrorNonNull(
    context: Context,
    isUserDriven: Boolean,
    owner: LifecycleOwner
) = observeErrorNonNull(context, owner, EMPTY_STRING, EMPTY_STRING, isUserDriven, null)

fun LiveData<Exception>.observeNetworkDrivenErrorNonNull(
    context: Context,
    owner: LifecycleOwner,
    defaultErrorTitle: String,
    defaultErrorMessage: String,
    isUserDriven: Boolean,
    observer: ((t: Exception) -> Unit)?
) {
    if (UtilFunctions.isNetworkAvailable(context, true)) {
        observeErrorNonNull(
            context,
            owner,
            defaultErrorTitle,
            defaultErrorMessage,
            isUserDriven,
            observer
        )
    }
}

fun Boolean?.toInt() = if (this != null && this) 1 else 0

fun Long.getElapsedTime(context: Context): String {
    var elapsed = this / 60
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
    return context.getString(R.string.description_last_checked, elapsed.toInt().toString(), suffix)
}

fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

fun Context.validateEmail(emailValue: String?, editText: EditText) {
    editText.setOnFocusChangeListener { _, hasFocus ->
        if (!hasFocus) {
            if (!emailValue.isNullOrEmpty() &&
                !UtilFunctions.isValidField(EMAIL_REGEX, emailValue)
            ) {
                editText.error = getString(R.string.incorrect_email_text)
            } else {
                editText.error = null
            }
        }
    }
}

fun Context.validatePassword(passwordValue: String?, editText: EditText) {
    editText.setOnFocusChangeListener { _, hasFocus ->
        if (!hasFocus) {
            if (!passwordValue.isNullOrEmpty() &&
                !UtilFunctions.isValidField(
                    PASSWORD_REGEX,
                    passwordValue
                )
            ) {
                editText.error =
                    getString(R.string.password_description)
            } else {
                editText.error = null
            }
        }
    }
}

fun View.setSafeOnClickListener(onSafeClick: (View) -> Unit) {
    val safeClickListener = SafeClickListener {
        onSafeClick(it)
    }
    setOnClickListener(safeClickListener)
}

fun matchSeparator(separatorId: Int, parentLayout: ConstraintLayout) {
    val constraintSet = ConstraintSet()
    constraintSet.clone(parentLayout)
    constraintSet.connect(
        separatorId,
        ConstraintSet.END,
        parentLayout.id,
        ConstraintSet.START,
        0
    )
    constraintSet.connect(
        separatorId,
        ConstraintSet.START,
        parentLayout.id,
        ConstraintSet.END,
        0
    )
    constraintSet.applyTo(parentLayout)
}

fun Intent.putSessionHandlerNavigationDestination(destination: String) {
    putExtra(SESSION_HANDLER_NAVIGATION_KEY, destination)
}

fun Intent.getSessionHandlerNavigationDestination(): String =
    getStringExtra(SESSION_HANDLER_NAVIGATION_KEY) ?: EMPTY_STRING

fun CardBalance?.formatBalance(): String {
    val balanceValue = this?.value?.toFloat() ?: 0f
    val balanceDecimalValue = balanceValue - balanceValue.toInt()
    return if (balanceDecimalValue != 0f) {
        this?.prefix?.plus(TWO_DECIMALS_FLOAT_FORMAT.format(Locale.ENGLISH, this.value?.toFloat()))
            .toString()
    } else {
        this?.prefix?.plus(balanceValue.toInt())
            .toString()
    }
}

fun logError(tag: String?, message: String?, exception: Exception? = null) {
    if (BuildConfig.BUILD_TYPE.lowercase() != BuildTypes.RELEASE.type) {
        tag?.let {
            message?.let {
                Log.e(tag, message, exception)
            }
        }
    }
}

fun logDebug(tag: String?, message: String?) {
    if (BuildConfig.BUILD_TYPE.lowercase() != BuildTypes.RELEASE.type) {
        message?.let {
            Log.d(tag, it)
        }
    }
}

fun TextView.setTermsAndPrivacyUrls(
    checkBoxText: String,
    termsAndConditions: String,
    privacyPolicy: String,
    urlClickListener: (String) -> Unit
) {
    val checkBoxSpannable = SpannableString(checkBoxText)
    checkBoxSpannable.setSpan(
        object : ClickableSpan() {
            override fun onClick(widget: View) {
                urlClickListener(TERMS_AND_CONDITIONS_URL)
            }
        },
        checkBoxText.indexOf(termsAndConditions, ignoreCase = true),
        checkBoxText.indexOf(termsAndConditions, ignoreCase = true) + termsAndConditions.length,
        Spannable.SPAN_INCLUSIVE_INCLUSIVE
    )
    checkBoxSpannable.setSpan(
        object : ClickableSpan() {
            override fun onClick(widget: View) {
                urlClickListener(PRIVACY_POLICY_URL)
            }
        },
        checkBoxText.indexOf(privacyPolicy, ignoreCase = true),
        checkBoxText.indexOf(privacyPolicy, ignoreCase = true) + privacyPolicy.length,
        Spannable.SPAN_INCLUSIVE_INCLUSIVE
    )
    text = checkBoxSpannable
    movementMethod = LinkMovementMethod.getInstance()
}

fun TextView.setMagicLinkUrl(
    magicLinkText: String,
    urlText: String,
    urlClickListener: (String) -> Unit
) {
    val magicLinkSpannable = SpannableString(magicLinkText)
    magicLinkSpannable.setSpan(
        object : ClickableSpan() {
            override fun onClick(widget: View) {
                urlClickListener(MAGIC_LINK_URL)
            }
        },
        magicLinkText.indexOf(urlText, ignoreCase = true),
        magicLinkText.indexOf(urlText, ignoreCase = true) + urlText.length,
        Spannable.SPAN_INCLUSIVE_INCLUSIVE
    )

    text = magicLinkSpannable
    movementMethod = LinkMovementMethod.getInstance()
}

fun HttpException.getErrorBody(): String {
    val errorBody = response()?.errorBody()?.string() ?: "Error body is null or empty"

    try {
        val jsonObject = JSONObject(errorBody)
        val keys = jsonObject.keys()

        while (keys.hasNext()) {
            val key = keys.next()
            return jsonObject.getString(key)
        }

    } catch (e: JSONException) {
        return "Could not deserialise error body. Error: ${e.message}. Raw Body: $errorBody"
    }

    return errorBody
}

fun String.readFileText(context: Context): String? {
    return try {
        context.assets?.open(this)?.bufferedReader().use {
            it?.readText() ?: "JS Error"
        }
    } catch (e: Exception) {
        return null
    }
}

fun Fragment.showUnLinkErrorMessage(errorText: String) {

    val title =
        getString(R.string.pll_error_title)

    AlertDialog.Builder(requireContext())
        .setTitle(title)
        .setMessage(errorText)
        .setPositiveButton(
            getString(R.string.ok)
        ) { dialog, _ ->
            dialog.dismiss()
        }
        .setCancelable(false)
        .show()
}

fun MembershipPlan.canPlanBeAdded(): Boolean {
    return this.feature_set?.linking_support?.contains(LINKING_SUPPORT_ADD) == true || this.feature_set?.linking_support?.contains(
        LINKING_SUPPORT_ENROL
    ) == true
}

fun Fragment.getMainActivity(): MainActivity {
    return requireActivity() as MainActivity
}

fun isProduction() = BuildConfig.BUILD_TYPE.lowercase() == BuildTypes.RELEASE.type

fun Context.showDialog(
    title: String? = null,
    message: String? = null,
    positiveBtn: String? = null,
    negativeBtn: String? = null,
    cancelable: Boolean = false,
    positiveCallback: () -> Unit = {},
    negativeCallback: () -> Unit = {}
) {
    val builder = androidx.appcompat.app.AlertDialog.Builder(this)
    builder.apply {
        setTitle(title)
        setMessage(message)
        setPositiveButton(positiveBtn) { _, _ ->
            positiveCallback()
        }
        setNegativeButton(negativeBtn) { _, _ ->
            negativeCallback()
        }
        setCancelable(cancelable)
        create()
    }
    builder.show()
}

inline fun Modifier.noRippleClickable(crossinline onClick: () -> Unit): Modifier = composed {
    clickable(indication = null,
        interactionSource = remember { MutableInteractionSource() }) {
        onClick()
    }
}



