package com.bink.wallet.utils

import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import android.widget.CheckBox
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
        textView: CheckBox
    ) {
        val spannableString = SpannableString(stringToSpan)
        spannableString.setSpan(
            URLSpan(url),
            spannableString.indexOf(stringToHyperlink),
            spannableString.indexOf(stringToHyperlink) + stringToHyperlink.length,
            Spanned.SPAN_INCLUSIVE_EXCLUSIVE
        )
        textView.text = spannableString
        textView.movementMethod = LinkMovementMethod.getInstance()
    }
}