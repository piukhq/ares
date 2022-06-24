package com.bink.wallet.utils

import android.text.Editable
import android.text.TextWatcher

interface SimplifiedTextWatcher : TextWatcher {
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        //do nothing
    }

    override fun afterTextChanged(s: Editable?) {
        //do nothing
    }
}