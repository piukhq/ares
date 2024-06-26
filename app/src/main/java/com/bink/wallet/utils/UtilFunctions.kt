package com.bink.wallet.utils

import android.content.Context
import android.net.ConnectivityManager
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
import com.bink.wallet.R
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException

object UtilFunctions {

    @Throws(PatternSyntaxException::class)
    fun isValidField(regex: String?, fieldValue: String?): Boolean {
        if (regex != null && fieldValue != null)
            return Pattern.compile(regex).matcher(fieldValue).matches()
        if (regex.isNullOrEmpty())
            return true
        return false
    }

    fun buildHyperlinkSpanString(
        stringToSpan: String,
        stringToHyperlink: String,
        url: String,
        textView: TextView,
        onLinkClickListener: ((String) -> Unit)? = null
    ) {
        val spannableString = SpannableString(stringToSpan)
        if (spannableString.contains(stringToHyperlink)) {
            spannableString.setSpan(
                object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        onLinkClickListener?.invoke(url)
                    }
                },
                spannableString.indexOf(stringToHyperlink),
                spannableString.indexOf(stringToHyperlink) + stringToHyperlink.length,
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE
            )
        }
        textView.text = spannableString
        textView.movementMethod = LinkMovementMethod.getInstance()
    }

    fun buildHyperlinkSpanStringWithoutUrl(
        stringToSpan: String,
        stringToHyperlink: String,
        textView: TextView,
        hyperLinkClickListener: (() -> Unit)? = null
    ): SpannableString {
        val spannableString = SpannableString(stringToSpan)
        if (spannableString.contains(stringToHyperlink)) {
            spannableString.setSpan(
                object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        hyperLinkClickListener?.invoke()
                    }
                },
                spannableString.indexOf(stringToHyperlink),
                spannableString.indexOf(stringToHyperlink) + stringToHyperlink.length,
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE
            )
        }
        textView.text = spannableString
        textView.movementMethod = LinkMovementMethod.getInstance()

        return spannableString
    }

    @Suppress("DEPRECATION")
    fun isNetworkAvailable(
        context: Context?,
        isUserAction: Boolean = false,
        okButtonAction: () -> Unit = {}
    ): Boolean {
        val connectivityManager =
            context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        val isNetworkAvailable = networkInfo != null && networkInfo.isConnected
        if (isUserAction && !isNetworkAvailable) {
            showNoInternetConnectionDialog(context, okButtonAction)
        }
        return isNetworkAvailable
    }

    private fun showNoInternetConnectionDialog(context: Context, okButtonAction: () -> Unit = {}) {
        context.displayModalPopup(
            null,
            context.getString(R.string.no_internet_connection_dialog_message),
            okButtonAction
        )
    }

    fun hasCertificatePinningFailed(error: Exception): Boolean {
        return error.toString().contains(CERT_PINNING_GENERAL_ERROR) ||
                error.message?.contains(
                    CERT_PINNING_GENERAL_ERROR
                ) == true
    }

    fun showCertificatePinningDialog(context: Context) {
        context.displayModalPopup(
            context.getString(R.string.ssl_pinning_failure_title),
            context.getString(R.string.ssl_pinning_failure_text)
        )
    }

}