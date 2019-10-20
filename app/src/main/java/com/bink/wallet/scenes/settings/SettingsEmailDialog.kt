package com.bink.wallet.scenes.settings

import android.content.Context
import android.util.Patterns
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import com.bink.wallet.R
import com.bink.wallet.databinding.SettingsChangeEmailDialogBinding

class SettingsEmailDialog(context: Context, private val initialEmail: String) :
    AlertDialog(context) {

    val newEmail = MutableLiveData<String>()

    init {
        val inflater = layoutInflater
        val binding: SettingsChangeEmailDialogBinding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.settings_change_email_dialog,
                null,
                false
            )

        setCancelable(true)
        setTitle(context.getString(R.string.edit_email_address))
        binding.email = initialEmail
        setView(binding.root)
        setButton(BUTTON_POSITIVE, context.getString(R.string.ok))
        { _, _ ->
            if (!setEmail(binding.email!!)) {
                binding.textInputLayout.error =
                    context.getString(R.string.please_enter_valid_email)
            }
        }
        setButton(BUTTON_NEGATIVE, context.getString(R.string.cancel_text))
        { _, _ -> dismiss() }
    }

    private fun setEmail(email: String): Boolean {
        if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            if (email != initialEmail) {
                newEmail.value = email
            }
            return true
        }
        return false
    }

}