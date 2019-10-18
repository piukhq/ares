package com.bink.wallet.utils

import android.app.Dialog
import android.content.Context
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import android.view.LayoutInflater
import androidx.databinding.DataBindingUtil
import com.bink.wallet.R
import com.bink.wallet.databinding.DialogSecurityBinding

class SecurityDialogs(val context: Context) {

    private var dialog: Dialog = Dialog(context)

    fun openDialog(layoutInflater: LayoutInflater) {
        val stringToSpan = context.getString(R.string.security_modal_body_3)
        val spannableString = SpannableStringBuilder(stringToSpan)
        val url = context.getString(R.string.terms_and_conditions_url)
        val hyperlinkText = context.getString(R.string.hyperlink_text)
        spannableString.setSpan(
            URLSpan(url),
            stringToSpan.indexOf(hyperlinkText),
            stringToSpan.indexOf(hyperlinkText) + hyperlinkText.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        val dialogBinding = DataBindingUtil.inflate<DialogSecurityBinding>(
            layoutInflater,
            R.layout.dialog_security,
            null,
            true
        )

        with(dialog) {
            if (!isShowing) {
                setContentView(dialogBinding.root)
                setTitle(R.string.security_modal_title)
                dialogBinding.preBody.text = context.getString(
                    R.string.security_modal_body,
                    context.getString(R.string.security_modal_body_1),
                    context.getString(R.string.security_modal_body_2)
                )
                dialogBinding.body.text = spannableString
                dialogBinding.body.movementMethod = LinkMovementMethod.getInstance()
                dialogBinding.ok.setOnClickListener {
                    dismiss()
                }
                show()
            }
        }
    }
}