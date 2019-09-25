package com.bink.wallet.scenes.settings

import androidx.fragment.app.DialogFragment
import android.os.Bundle
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import com.bink.wallet.R

class SettingsEditEmailDialog: DialogFragment() {
    fun newInstance(email: String): SettingsEditEmailDialog {
        val frag = SettingsEditEmailDialog()
        val args = Bundle()
        args.putString("email", email)
        frag.arguments = args
        return frag
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.settings_change_email_dialog, container)
        return view
    }
}