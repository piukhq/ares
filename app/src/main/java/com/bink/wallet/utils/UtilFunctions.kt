package com.bink.wallet.utils

import android.content.Context
import android.net.ConnectivityManager
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import android.widget.CheckBox
import com.bink.wallet.R
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException

object UtilFunctions {

    @Throws(PatternSyntaxException::class)
    fun isValidField(regex: String?, fieldValue: String?): Boolean {
        if (regex != null && fieldValue != null)
            return Pattern.compile(regex.let { it }).matcher(fieldValue.let { it }).matches()
        if (regex == null || regex.isNullOrEmpty())
            return true
        return false
    }

    fun buildHyperlinkSpanString(
        stringToSpan: String,
        stringToHyperlink: String,
        url: String,
        checkBox: CheckBox
    ) {
        val spannableString = SpannableString(stringToSpan)
        if (spannableString.contains(stringToHyperlink)) {
            spannableString.setSpan(
                URLSpan(url),
                spannableString.indexOf(stringToHyperlink),
                spannableString.indexOf(stringToHyperlink) + stringToHyperlink.length,
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE
            )
        }
        checkBox.apply {
            text = spannableString
            movementMethod = LinkMovementMethod.getInstance()
        }
    }

    fun isNetworkAvailable(context: Context, isUserAction: Boolean): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        val isNetworkAvailable = networkInfo != null && networkInfo.isConnected
        if (isUserAction && !isNetworkAvailable) {
            showNoInternetConnectionDialog(context)
        }
        return isNetworkAvailable
    }

    private fun showNoInternetConnectionDialog(context: Context) {
        context.displayModalPopup(
            null,
            context.getString(R.string.no_internet_connection_dialog_message)
        )
    }
}